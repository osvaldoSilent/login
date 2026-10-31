package com.osvaldevops.login.domain.event;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String email,
        String userName,
        String role,
        Instant createdAt
) {}

