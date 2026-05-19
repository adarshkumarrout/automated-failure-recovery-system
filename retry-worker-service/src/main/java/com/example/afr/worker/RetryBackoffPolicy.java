package com.example.afr.worker;

import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class RetryBackoffPolicy {

    private final RecoveryProperties properties;

    public RetryBackoffPolicy(RecoveryProperties properties) {
        this.properties = properties;
    }

    public Duration delayForAttempt(int attemptCount) {
        long initialDelay = properties.getRetry().getInitialDelaySeconds();
        long maxDelay = properties.getRetry().getMaxDelaySeconds();
        long exponentialDelay = initialDelay * (1L << Math.max(attemptCount - 1, 0));
        return Duration.ofSeconds(Math.min(exponentialDelay, maxDelay));
    }
}

