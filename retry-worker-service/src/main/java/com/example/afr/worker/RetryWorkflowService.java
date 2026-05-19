package com.example.afr.worker;

import com.example.afr.common.RecoveryEvent;
import com.example.afr.common.RecoveryEventFactory;
import com.example.afr.common.RecoveryRequestEntity;
import com.example.afr.common.RecoveryRequestRepository;
import com.example.afr.common.RecoveryStatus;
import com.example.afr.common.RecoveryTopics;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetryWorkflowService {

    private final RecoveryRequestRepository repository;
    private final RetryBackoffPolicy backoffPolicy;
    private final PaymentClient paymentClient;
    private final KafkaTemplate<String, RecoveryEvent> kafkaTemplate;
    private final RecoveryProperties properties;
    private final RecoveryMetrics metrics;

    public RetryWorkflowService(
            RecoveryRequestRepository repository,
            RetryBackoffPolicy backoffPolicy,
            PaymentClient paymentClient,
            KafkaTemplate<String, RecoveryEvent> kafkaTemplate,
            RecoveryProperties properties,
            RecoveryMetrics metrics
    ) {
        this.repository = repository;
        this.backoffPolicy = backoffPolicy;
        this.paymentClient = paymentClient;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.metrics = metrics;
    }

    @Transactional
    public void scheduleRetry(String idempotencyKey, String reason) {
        RecoveryRequestEntity request = get(idempotencyKey);
        if (request.getStatus() == RecoveryStatus.SUCCEEDED
                || request.getStatus() == RecoveryStatus.DEAD_LETTER
                || request.getStatus() == RecoveryStatus.PERMANENT_FAILURE) {
            return;
        }

        if (request.getAttemptCount() >= properties.getRetry().getMaxAttempts()) {
            moveToDeadLetter(request, "Max retry attempts exhausted: " + reason);
            return;
        }

        request.setStatus(RecoveryStatus.RETRY_PENDING);
        request.setLastError(reason);
        request.setNextRetryAt(Instant.now().plus(backoffPolicy.delayForAttempt(request.getAttemptCount() + 1)));
        repository.save(request);
    }

    @Transactional
    public void executeRetry(String idempotencyKey) {
        RecoveryRequestEntity request = get(idempotencyKey);
        if (request.getStatus() != RecoveryStatus.RETRY_PENDING) {
            return;
        }
        if (request.getNextRetryAt() != null && request.getNextRetryAt().isAfter(Instant.now())) {
            return;
        }

        request.setStatus(RecoveryStatus.RETRYING);
        request.incrementAttemptCount();
        repository.save(request);

        try {
            PaymentClient.PaymentExecutionResponse response = paymentClient.execute(request);
            if (response != null && response.success()) {
                request.setStatus(RecoveryStatus.SUCCEEDED);
                request.setNextRetryAt(null);
                request.setLastError(null);
                repository.save(request);
                metrics.retrySucceeded();
                publish(RecoveryTopics.RETRY_SUCCEEDED, request, "Retry succeeded");
                return;
            }

            boolean retryable = response == null || response.retryable();
            String message = response == null ? "Payment service returned empty response" : response.message();
            handleFailure(request, retryable, message);
        } catch (Exception exception) {
            handleFailure(request, true, exception.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<RecoveryRequestEntity> dueRetries(Instant now) {
        return repository.findTop100ByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
                RecoveryStatus.RETRY_PENDING,
                now
        );
    }

    private void handleFailure(RecoveryRequestEntity request, boolean retryable, String message) {
        request.setLastError(message);
        if (!retryable) {
            request.setStatus(RecoveryStatus.PERMANENT_FAILURE);
            request.setNextRetryAt(null);
            repository.save(request);
            metrics.deadLettered();
            publish(RecoveryTopics.DEAD_LETTER, request, message);
            return;
        }

        request.setStatus(RecoveryStatus.RETRY_PENDING);
        repository.save(request);
        metrics.retryFailed();
        publish(RecoveryTopics.RETRY_FAILED, request, message);
        scheduleRetry(request.getIdempotencyKey(), message);
    }

    private RecoveryRequestEntity get(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new EntityNotFoundException("No recovery request found for key: " + idempotencyKey));
    }

    private void moveToDeadLetter(RecoveryRequestEntity request, String reason) {
        request.setStatus(RecoveryStatus.DEAD_LETTER);
        request.setNextRetryAt(null);
        request.setLastError(reason);
        repository.save(request);
        metrics.deadLettered();
        publish(RecoveryTopics.DEAD_LETTER, request, reason);
    }

    private void publish(String topic, RecoveryRequestEntity request, String message) {
        kafkaTemplate.send(topic, request.getIdempotencyKey(), RecoveryEventFactory.from(request, message));
    }
}

