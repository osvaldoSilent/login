package com.osvaldevops.login.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.lang.NonNull;

/**
 * Apache Kafka topic configuration for user lifecycle events.
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * Declares the Kafka topic used to publish {@code UserCreatedEvent} messages.
     */
    @Bean
    public NewTopic userCreatedTopic(
            @Value("${app.kafka.topics.user-created:user-created-events}") @NonNull String topicName
    ) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
