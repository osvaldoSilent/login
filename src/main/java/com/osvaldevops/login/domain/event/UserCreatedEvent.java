package com.osvaldevops.login.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a new user account is successfully created.
 *
 * @param userId    unique identifier (UUID) of the newly created user
 * @param email     registered email address of the user
 * @param userName  display username of the user
 * @param role      assigned authorization role
 * @param createdAt timestamp when the user was created
 */
public record UserCreatedEvent(
        UUID userId,
        String email,
        String userName,
        String role,
        Instant createdAt
) {}
