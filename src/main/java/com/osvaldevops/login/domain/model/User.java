package com.osvaldevops.login.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Core domain model representing a registered user within the system.
 */
@Getter
@Builder
@AllArgsConstructor
public class User {

    private final UUID id;
    private final String email;
    private final String password;
    private final String userName;
    private final String role;
    private final boolean enabled;
    private final Instant createdAt;
    private final Instant updatedAt;
}
