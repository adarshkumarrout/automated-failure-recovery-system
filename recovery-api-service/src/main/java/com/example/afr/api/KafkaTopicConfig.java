package com.example.afr.api;

import com.example.afr.common.RecoveryTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic apiFailedEventsTopic() {
        return topic(RecoveryTopics.API_FAILED);
    }

    @Bean
    NewTopic retryScheduledEventsTopic() {
        return topic(RecoveryTopics.RETRY_SCHEDULED);
    }

    @Bean
    NewTopic retrySucceededEventsTopic() {
        return topic(RecoveryTopics.RETRY_SUCCEEDED);
    }

    @Bean
    NewTopic retryFailedEventsTopic() {
        return topic(RecoveryTopics.RETRY_FAILED);
    }

    @Bean
    NewTopic deadLetterEventsTopic() {
        return topic(RecoveryTopics.DEAD_LETTER);
    }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }
}

