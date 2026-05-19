package com.example.afr.worker;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;
    private final RecoveryProperties properties;

    public DistributedLockService(StringRedisTemplate redisTemplate, RecoveryProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public boolean acquire(String idempotencyKey) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                lockKey(idempotencyKey),
                "locked",
                Duration.ofSeconds(properties.getLock().getTtlSeconds())
        );
        return Boolean.TRUE.equals(acquired);
    }

    public void release(String idempotencyKey) {
        redisTemplate.delete(lockKey(idempotencyKey));
    }

    private String lockKey(String idempotencyKey) {
        return "recovery:lock:" + idempotencyKey;
    }
}

