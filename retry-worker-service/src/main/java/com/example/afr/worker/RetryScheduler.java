package com.example.afr.worker;

import com.example.afr.common.RecoveryEvent;
import com.example.afr.common.RecoveryEventFactory;
import com.example.afr.common.RecoveryRequestEntity;
import com.example.afr.common.RecoveryTopics;
import java.time.Instant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryScheduler {

    private final RetryWorkflowService workflowService;
    private final KafkaTemplate<String, RecoveryEvent> kafkaTemplate;

    public RetryScheduler(RetryWorkflowService workflowService, KafkaTemplate<String, RecoveryEvent> kafkaTemplate) {
        this.workflowService = workflowService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${recovery.retry.scheduler-rate-ms}")
    public void publishDueRetries() {
        for (RecoveryRequestEntity request : workflowService.dueRetries(Instant.now())) {
            kafkaTemplate.send(
                    RecoveryTopics.RETRY_SCHEDULED,
                    request.getIdempotencyKey(),
                    RecoveryEventFactory.from(request, "Due retry published by scheduler")
            );
        }
    }
}

