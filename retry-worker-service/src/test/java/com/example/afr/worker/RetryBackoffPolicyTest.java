package com.example.afr.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RetryBackoffPolicyTest {

    @Test
    void calculatesExponentialBackoffWithCap() {
        RecoveryProperties properties = new RecoveryProperties();
        properties.getRetry().setInitialDelaySeconds(5);
        properties.getRetry().setMaxDelaySeconds(20);

        RetryBackoffPolicy policy = new RetryBackoffPolicy(properties);

        assertThat(policy.delayForAttempt(1)).isEqualTo(Duration.ofSeconds(5));
        assertThat(policy.delayForAttempt(2)).isEqualTo(Duration.ofSeconds(10));
        assertThat(policy.delayForAttempt(3)).isEqualTo(Duration.ofSeconds(20));
        assertThat(policy.delayForAttempt(4)).isEqualTo(Duration.ofSeconds(20));
    }
}

