package com.example.afr.api;

import com.example.afr.common.FailureType;
import com.example.afr.common.RecoveryRequestEntity;
import com.example.afr.common.RecoveryStatus;
import java.time.Instant;

public record RecoveryResponse(
        String idempotencyKey,
        String operationType,
        RecoveryStatus status,
        FailureType failureType,
        int attemptCount,
        Instant nextRetryAt,
        String lastError,
        Instant createdAt,
        Instant updatedAt
) {
    public static RecoveryResponse from(RecoveryRequestEntity request) {
        return new RecoveryResponse(
                request.getIdempotencyKey(),
                request.getOperationType(),
                request.getStatus(),
                request.getFailureType(),
                request.getAttemptCount(),
                request.getNextRetryAt(),
                request.getLastError(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}

