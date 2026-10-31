package com.osvaldevops.login.infrastructure.adapter.out.messaging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.osvaldevops.login.domain.event.UserCreatedEvent;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class KafkaUserEventPublisherAdapterTest {

    private static final String TOPIC = "user-created-events";

    @Mock
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    private KafkaUserEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new KafkaUserEventPublisherAdapter(kafkaTemplate, TOPIC);
    }

    @Test
    @DisplayName("Should send UserCreatedEvent to configured Kafka topic using userId as key")
    void shouldSendUserCreatedEventToKafkaTopic() {
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "osval@osvaldevops.com",
                "osval",
                "ROLE_USER",
                Instant.now()
        );

        CompletableFuture<SendResult<String, UserCreatedEvent>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(TOPIC, userId.toString(), event)).thenReturn(future);

        adapter.publishUserCreated(event);

        verify(kafkaTemplate).send(TOPIC, userId.toString(), event);
    }
}

