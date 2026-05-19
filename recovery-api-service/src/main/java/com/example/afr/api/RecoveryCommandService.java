package com.example.afr.api;

import com.example.afr.common.FailureType;
import com.example.afr.common.RecoveryEvent;
import com.example.afr.common.RecoveryEventFactory;
import com.example.afr.common.RecoveryRequestEntity;
import com.example.afr.common.RecoveryRequestRepository;
import com.example.afr.common.RecoveryStatus;
import com.example.afr.common.RecoveryTopics;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecoveryCommandService {

    private final RecoveryRequestRepository repository;
    private final KafkaTemplate<String, RecoveryEvent> kafkaTemplate;

    public RecoveryCommandService(
            RecoveryRequestRepository repository,
            KafkaTemplate<String, RecoveryEvent> kafkaTemplate
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public RecoveryRequestEntity execute(ExecuteRecoveryRequest command) {
        return repository.findByIdempotencyKey(command.idempotencyKey())
                .orElseGet(() -> createAndPublish(command));
    }

    @Transactional(readOnly = true)
    public RecoveryRequestEntity get(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new EntityNotFoundException("No recovery request found for key: " + idempotencyKey));
    }

    @Transactional(readOnly = true)
    public List<RecoveryRequestEntity> recent() {
        return repository.findTop50ByOrderByUpdatedAtDesc();
    }

    private RecoveryRequestEntity createAndPublish(ExecuteRecoveryRequest command) {
        RecoveryRequestEntity request = RecoveryRequestEntity.create(
                command.idempotencyKey(),
                command.operationType(),
                command.payload(),
                command.failureMode(),
                command.failuresBeforeSuccess()
        );
        request.setStatus(RecoveryStatus.RETRY_PENDING);

        if (command.failureMode() == FailureType.PERMANENT) {
            request.setStatus(RecoveryStatus.PERMANENT_FAILURE);
            request.setLastError("Permanent failure submitted for recovery");
            RecoveryRequestEntity saved = repository.save(request);
            publish(RecoveryTopics.DEAD_LETTER, saved, "Permanent failure");
            return saved;
        }

        RecoveryRequestEntity saved = repository.save(request);
        publish(RecoveryTopics.API_FAILED, saved, "Recovery request accepted");
        return saved;
    }

    private void publish(String topic, RecoveryRequestEntity request, String message) {
        kafkaTemplate.send(topic, request.getIdempotencyKey(), RecoveryEventFactory.from(request, message));
    }
}

