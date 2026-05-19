package com.example.afr.worker;

import com.example.afr.common.RecoveryEvent;
import com.example.afr.common.RecoveryTopics;
import java.time.Instant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RetryEventListener {

    private final RetryWorkflowService workflowService;
    private final DistributedLockService lockService;

    public RetryEventListener(RetryWorkflowService workflowService, DistributedLockService lockService) {
        this.workflowService = workflowService;
        this.lockService = lockService;
    }

    @KafkaListener(topics = RecoveryTopics.RETRY_SCHEDULED)
    public void onRetryScheduled(RecoveryEvent event) {
        if (event.nextRetryAt() != null && event.nextRetryAt().isAfter(Instant.now())) {
            return;
        }
        if (!lockService.acquire(event.idempotencyKey())) {
            return;
        }

        try {
            workflowService.executeRetry(event.idempotencyKey());
        } finally {
            lockService.release(event.idempotencyKey());
        }
    }
}

