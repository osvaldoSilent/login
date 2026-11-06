package com.osvaldevops.login.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representing a newly registered user account.
 */
@Schema(description = "Response payload representing a registered user")
public record UserResponse(
        @Schema(description = "Unique identifier (UUID) of the user", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @Schema(description = "Email address of the user", example = "osval@osvaldevops.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @Schema(description = "Username of the user", example = "osval", requiredMode = Schema.RequiredMode.REQUIRED)
        String userName,

        @Schema(description = "Assigned authorization role", example = "ROLE_USER", requiredMode = Schema.RequiredMode.REQUIRED)
        String role,

        @Schema(description = "Indicates whether the user account is active", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean enabled,

        @Schema(description = "Timestamp when the user was created", example = "2026-10-30T18:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
        Instant createdAt
) {}
