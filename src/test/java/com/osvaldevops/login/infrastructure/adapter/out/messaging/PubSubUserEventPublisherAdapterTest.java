package com.osvaldevops.login.infrastructure.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.osvaldevops.login.domain.event.UserCreatedEvent;
import com.osvaldevops.login.domain.exception.EventPublicationException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link PubSubUserEventPublisherAdapter}.
 */
@ExtendWith(MockitoExtension.class)
class PubSubUserEventPublisherAdapterTest {

    private static final String TOPIC = "user-created-topic";

    @Mock
    private PubSubTemplate pubSubTemplate;

    private ObjectMapper objectMapper;
    private PubSubUserEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        adapter = new PubSubUserEventPublisherAdapter(pubSubTemplate, objectMapper, TOPIC);
    }

    @Test
    @DisplayName("Should serialize UserCreatedEvent, publish to GCP Pub/Sub with attributes, and handle success callback")
    @SuppressWarnings("unchecked")
    void should_PublishUserCreatedEventToPubSub_When_EventIsValid() {
        // Given
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "osval@osvaldevops.com",
                "osval",
                "ROLE_USER",
                Instant.parse("2026-11-04T23:30:00Z")
        );

        CompletableFuture<String> future = CompletableFuture.completedFuture("pubsub-msg-id-123");
        when(pubSubTemplate.publish(eq(TOPIC), anyString(), anyMap())).thenReturn(future);

        // When
        adapter.publishUserCreated(event);

        // Then
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, String>> attributesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pubSubTemplate).publish(eq(TOPIC), payloadCaptor.capture(), attributesCaptor.capture());

        assertThat(payloadCaptor.getValue()).contains("osval@osvaldevops.com", userId.toString(), "ROLE_USER");
        assertThat(attributesCaptor.getValue())
                .containsEntry("eventType", "USER_CREATED")
                .containsEntry("userId", userId.toString());
    }

    @Test
    @DisplayName("Should use email as userId attribute when userId is null and handle async failure callback")
    @SuppressWarnings("unchecked")
    void should_UseEmailAsUserIdAttribute_When_UserIdIsNullAndHandleAsyncFailure() {
        // Given
        UserCreatedEvent event = new UserCreatedEvent(
                null,
                "osval@osvaldevops.com",
                "osval",
                "ROLE_USER",
                Instant.parse("2026-11-04T23:30:00Z")
        );

        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("GCP Pub/Sub unavailable"));
        when(pubSubTemplate.publish(eq(TOPIC), anyString(), anyMap())).thenReturn(future);

        // When
        adapter.publishUserCreated(event);

        // Then
        ArgumentCaptor<Map<String, String>> attributesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(pubSubTemplate).publish(eq(TOPIC), anyString(), attributesCaptor.capture());
        assertThat(attributesCaptor.getValue())
                .containsEntry("eventType", "USER_CREATED")
                .containsEntry("userId", "osval@osvaldevops.com");
    }

    @Test
    @DisplayName("Should throw EventPublicationException when PubSubTemplate throws RuntimeException synchronously")
    void should_ThrowEventPublicationException_When_PubSubPublishFailsSynchronously() {
        // Given
        UUID userId = UUID.randomUUID();
        UserCreatedEvent event = new UserCreatedEvent(
                userId,
                "osval@osvaldevops.com",
                "osval",
                "ROLE_USER",
                Instant.now()
        );

        when(pubSubTemplate.publish(eq(TOPIC), anyString(), anyMap()))
                .thenThrow(new RuntimeException("GCP credentials invalid"));

        // When / Then
        assertThatThrownBy(() -> adapter.publishUserCreated(event))
                .isInstanceOf(EventPublicationException.class)
                .hasMessage("Error publishing UserCreatedEvent to GCP Pub/Sub")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw EventPublicationException when JSON serialization fails")
    void should_ThrowEventPublicationException_When_JsonSerializationFails() throws Exception {
        // Given
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        PubSubUserEventPublisherAdapter adapterWithFailingMapper = new PubSubUserEventPublisherAdapter(
                pubSubTemplate,
                failingMapper,
                TOPIC
        );
        UserCreatedEvent event = new UserCreatedEvent(
                UUID.randomUUID(),
                "osval@osvaldevops.com",
                "osval",
                "ROLE_USER",
                Instant.now()
        );

        JsonProcessingException jsonEx = mock(JsonProcessingException.class);
        when(failingMapper.writeValueAsString(event)).thenThrow(jsonEx);

        // When / Then
        assertThatThrownBy(() -> adapterWithFailingMapper.publishUserCreated(event))
                .isInstanceOf(EventPublicationException.class)
                .hasMessage("Error publishing UserCreatedEvent to GCP Pub/Sub")
                .hasCause(jsonEx);

        verify(pubSubTemplate, never()).publish(any(), anyString(), anyMap());
    }
}

