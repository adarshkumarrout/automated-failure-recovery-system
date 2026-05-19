package com.example.afr.worker;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "recovery")
public class RecoveryProperties {

    private Retry retry = new Retry();
    private Lock lock = new Lock();

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    public static class Retry {
        private int maxAttempts = 5;
        private long initialDelaySeconds = 5;
        private long maxDelaySeconds = 300;
        private long schedulerRateMs = 5000;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialDelaySeconds() {
            return initialDelaySeconds;
        }

        public void setInitialDelaySeconds(long initialDelaySeconds) {
            this.initialDelaySeconds = initialDelaySeconds;
        }

        public long getMaxDelaySeconds() {
            return maxDelaySeconds;
        }

        public void setMaxDelaySeconds(long maxDelaySeconds) {
            this.maxDelaySeconds = maxDelaySeconds;
        }

        public long getSchedulerRateMs() {
            return schedulerRateMs;
        }

        public void setSchedulerRateMs(long schedulerRateMs) {
            this.schedulerRateMs = schedulerRateMs;
        }
    }

    public static class Lock {
        private long ttlSeconds = 60;

        public long getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(long ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }
    }
}

