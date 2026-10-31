package com.osvaldevops.login.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String userName,
        String role,
        boolean enabled,
        Instant createdAt
) {}

