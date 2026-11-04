package com.osvaldevops.login.infrastructure.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.osvaldevops.login.domain.event.UserCreatedEvent;
import com.osvaldevops.login.domain.exception.EventPublicationException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
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
    @DisplayName("Should send UserCreatedEvent to configured Kafka topic using userId as key and handle success callback")
    @SuppressWarnings("unchecked")
    void shouldSendUserCreatedEventToKafkaTopic_andHandleSuccess() {
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "osval@osvaldevops.com",
                "osval",
                "ROLE_USER",
                Instant.now()
        );

        SendResult<String, UserCreatedEvent> sendResult = mock(SendResult.class);
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(TOPIC, 0), 0L, 42, 0L, 0, 0);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);

        CompletableFuture<SendResult<String, UserCreatedEvent>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(TOPIC, Objects.requireNonNull(userId.toString()), event)).thenReturn(future);

        adapter.publishUserCreated(event);

        verify(kafkaTemplate).send(TOPIC, Objects.requireNonNull(userId.toString()), event);
    }

    @Test
    @DisplayName("Should use email as key when userId is null and handle async failure callback")
    void shouldUseEmailAsKeyWhenUserIdIsNull_andHandleAsyncFailure() {
        UserCreatedEvent event = new UserCreatedEvent(
                null,
                "osval@osvaldevops.com",
                "osval",
                "ROLE_USER",
                Instant.now()
        );

        CompletableFuture<SendResult<String, UserCreatedEvent>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Broker disconnected"));
        when(kafkaTemplate.send(TOPIC, "osval@osvaldevops.com", event)).thenReturn(future);

        adapter.publishUserCreated(event);

        verify(kafkaTemplate).send(TOPIC, "osval@osvaldevops.com", event);
    }

    @Test
    @DisplayName("Should throw EventPublicationException when KafkaTemplate throws RuntimeException synchronously")
    void shouldThrowEventPublicationException_whenSendFailsSynchronously() {
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "osval@osvaldevops.com",
                "osval",
                "ROLE_USER",
                Instant.now()
        );

        when(kafkaTemplate.send(TOPIC, Objects.requireNonNull(userId.toString()), event))
                .thenThrow(new RuntimeException("Kafka producer closed"));

        assertThatThrownBy(() -> adapter.publishUserCreated(event))
                .isInstanceOf(EventPublicationException.class)
                .hasMessage("Error publishing UserCreatedEvent to Kafka")
                .hasCauseInstanceOf(RuntimeException.class);
    }
}
