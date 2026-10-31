package com.osvaldevops.login.domain.port.out;

import com.osvaldevops.login.domain.event.UserCreatedEvent;

/**
 * Outbound port for publishing user lifecycle domain events to an external message broker.
 */
public interface UserEventPublisherPort {

    /**
     * Publishes a {@link UserCreatedEvent} when a new user is registered.
     *
     * @param event domain event containing the created user's metadata
     */
    void publishUserCreated(UserCreatedEvent event);
}
