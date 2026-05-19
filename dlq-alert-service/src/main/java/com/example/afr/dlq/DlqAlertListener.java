package com.example.afr.dlq;

import com.example.afr.common.RecoveryEvent;
import com.example.afr.common.RecoveryTopics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DlqAlertListener {

    private static final Logger log = LoggerFactory.getLogger(DlqAlertListener.class);

    private final Counter dlqAlerts;

    public DlqAlertListener(MeterRegistry meterRegistry) {
        this.dlqAlerts = Counter.builder("dlq_alerts_total").register(meterRegistry);
    }

    @KafkaListener(topics = RecoveryTopics.DEAD_LETTER)
    public void onDeadLetter(RecoveryEvent event) {
        dlqAlerts.increment();
        log.warn(
                "DLQ alert idempotencyKey={} operation={} attempts={} message={}",
                event.idempotencyKey(),
                event.operationType(),
                event.attemptCount(),
                event.message()
        );
    }
}

