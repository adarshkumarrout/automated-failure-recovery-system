package com.example.afr.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(
        name = "recovery_requests",
        indexes = {
                @Index(name = "idx_recovery_idempotency_key", columnList = "idempotencyKey", unique = true),
                @Index(name = "idx_recovery_status_next_retry", columnList = "status,nextRetryAt")
        }
)
public class RecoveryRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Column(nullable = false, length = 80)
    private String operationType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RecoveryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FailureType failureType;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private int failuresBeforeSuccess;

    private Instant nextRetryAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    public static RecoveryRequestEntity create(
            String idempotencyKey,
            String operationType,
            String payload,
            FailureType failureType,
            int failuresBeforeSuccess
    ) {
        Instant now = Instant.now();
        RecoveryRequestEntity request = new RecoveryRequestEntity();
        request.idempotencyKey = idempotencyKey;
        request.operationType = operationType;
        request.payload = payload;
        request.status = RecoveryStatus.RECEIVED;
        request.failureType = failureType;
        request.failuresBeforeSuccess = Math.max(failuresBeforeSuccess, 0);
        request.createdAt = now;
        request.updatedAt = now;
        return request;
    }

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getPayload() {
        return payload;
    }

    public RecoveryStatus getStatus() {
        return status;
    }

    public void setStatus(RecoveryStatus status) {
        this.status = status;
        touch();
    }

    public FailureType getFailureType() {
        return failureType;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void incrementAttemptCount() {
        this.attemptCount++;
        touch();
    }

    public int getFailuresBeforeSuccess() {
        return failuresBeforeSuccess;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(Instant nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
        touch();
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
        touch();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }
}

