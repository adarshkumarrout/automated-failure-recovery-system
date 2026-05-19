package com.example.afr.worker;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class RecoveryMetrics {

    private final Counter retrySuccesses;
    private final Counter retryFailures;
    private final Counter deadLetters;

    public RecoveryMetrics(MeterRegistry meterRegistry) {
        this.retrySuccesses = Counter.builder("recovery_retry_success_total").register(meterRegistry);
        this.retryFailures = Counter.builder("recovery_retry_failure_total").register(meterRegistry);
        this.deadLetters = Counter.builder("recovery_dead_letter_total").register(meterRegistry);
    }

    public void retrySucceeded() {
        retrySuccesses.increment();
    }

    public void retryFailed() {
        retryFailures.increment();
    }

    public void deadLettered() {
        deadLetters.increment();
    }
}

