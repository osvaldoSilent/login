package com.osvaldevops.login.infrastructure.adapter.out.messaging;

import com.osvaldevops.login.domain.event.UserCreatedEvent;
import com.osvaldevops.login.domain.exception.EventPublicationException;
import com.osvaldevops.login.domain.port.out.UserEventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Outbound messaging adapter implementing {@link UserEventPublisherPort} to publish
 * {@link UserCreatedEvent} messages to Apache Kafka.
 */
@Slf4j
@Component
public class KafkaUserEventPublisherAdapter implements UserEventPublisherPort {

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
    private final String userCreatedTopic;

    public KafkaUserEventPublisherAdapter(
            KafkaTemplate<String, UserCreatedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.user-created:user-created-events}") String userCreatedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.userCreatedTopic = userCreatedTopic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishUserCreated(UserCreatedEvent event) {
        // Use userId as the Kafka partition key, falling back to email if userId is null
        String key = event.userId() != null ? event.userId().toString() : event.email();

        try {
            final String userCreatedTopic2 = userCreatedTopic;
            if (userCreatedTopic2 != null && key != null) {
                kafkaTemplate.send(userCreatedTopic2, key, event)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to publish UserCreatedEvent for email={} to topic={}",
                                        event.email(), userCreatedTopic, ex);
                            } else {
                                log.info("Published UserCreatedEvent for userId={} email={} to topic={} partition={} offset={}",
                                         event.userId(),
                                         event.email(),
                                         userCreatedTopic,
                                         result.getRecordMetadata().partition(),
                                         result.getRecordMetadata().offset());
                            }
                        });
            } else {
                // TODO handle null value
            }
        } catch (RuntimeException ex) {
            throw new EventPublicationException("Error publishing UserCreatedEvent to Kafka", ex);
        }
    }
}
