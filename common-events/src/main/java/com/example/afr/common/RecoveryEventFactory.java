package com.example.afr.common;

import java.time.Instant;
import java.util.UUID;

public final class RecoveryEventFactory {

    private RecoveryEventFactory() {
    }

    public static RecoveryEvent from(RecoveryRequestEntity request, String message) {
        return new RecoveryEvent(
                UUID.randomUUID().toString(),
                request.getIdempotencyKey(),
                request.getOperationType(),
                request.getStatus(),
                request.getFailureType(),
                request.getAttemptCount(),
                message,
                request.getNextRetryAt(),
                Instant.now()
        );
    }
}

