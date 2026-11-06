package com.osvaldevops.login.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.osvaldevops.login.domain.event.UserCreatedEvent;
import com.osvaldevops.login.domain.exception.EventPublicationException;
import com.osvaldevops.login.domain.port.out.UserEventPublisherPort;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Outbound messaging adapter implementing {@link UserEventPublisherPort} to publish
 * {@link UserCreatedEvent} messages to Google Cloud Pub/Sub.
 */
@Slf4j
@Component
public class PubSubUserEventPublisherAdapter implements UserEventPublisherPort {

    private static final String ATTR_EVENT_TYPE = "eventType";
    private static final String ATTR_USER_ID = "userId";
    private static final String EVENT_USER_CREATED = "USER_CREATED";

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;
    private final String userCreatedTopic;

    public PubSubUserEventPublisherAdapter(
            PubSubTemplate pubSubTemplate,
            ObjectMapper objectMapper,
            @Value("${app.pubsub.topics.user-created}") String userCreatedTopic
    ) {
        this.pubSubTemplate = pubSubTemplate;
        this.objectMapper = objectMapper;
        this.userCreatedTopic = userCreatedTopic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishUserCreated(UserCreatedEvent event) {
        String userKey = event.userId() != null ? event.userId().toString() : event.email();

        // Pre-size map for eventType and userId attributes to minimize heap allocations
        Map<String, String> attributes = new LinkedHashMap<>(2);
        attributes.put(ATTR_EVENT_TYPE, EVENT_USER_CREATED);
        attributes.put(ATTR_USER_ID, userKey);

        try {
            String payloadJson = objectMapper.writeValueAsString(event);

            pubSubTemplate.publish(userCreatedTopic, payloadJson, attributes)
                    .whenComplete((messageId, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish UserCreatedEvent for email={} to GCP Pub/Sub topic={}",
                                    event.email(), userCreatedTopic, ex);
                        } else {
                            log.info("Published UserCreatedEvent for userId={} email={} to GCP Pub/Sub topic={} messageId={}",
                                    event.userId(),
                                    event.email(),
                                    userCreatedTopic,
                                    messageId);
                        }
                    });
        } catch (JsonProcessingException | RuntimeException ex) {
            throw new EventPublicationException("Error publishing UserCreatedEvent to GCP Pub/Sub", ex);
        }
    }
}

