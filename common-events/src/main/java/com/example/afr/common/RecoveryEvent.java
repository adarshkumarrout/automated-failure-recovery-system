package com.example.afr.common;

import java.time.Instant;

public record RecoveryEvent(
        String eventId,
        String idempotencyKey,
        String operationType,
        RecoveryStatus status,
        FailureType failureType,
        int attemptCount,
        String message,
        Instant nextRetryAt,
        Instant occurredAt
) {
}

