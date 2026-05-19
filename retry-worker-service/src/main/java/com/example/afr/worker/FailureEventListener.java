package com.example.afr.worker;

import com.example.afr.common.RecoveryEvent;
import com.example.afr.common.RecoveryTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FailureEventListener {

    private final RetryWorkflowService workflowService;

    public FailureEventListener(RetryWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @KafkaListener(topics = RecoveryTopics.API_FAILED)
    public void onApiFailed(RecoveryEvent event) {
        workflowService.scheduleRetry(event.idempotencyKey(), event.message());
    }
}

