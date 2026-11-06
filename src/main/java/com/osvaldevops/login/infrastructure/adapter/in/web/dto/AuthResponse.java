package com.osvaldevops.login.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO containing the signed JWT token and expiration metadata upon successful login.
 */
@Schema(description = "Response payload containing the signed JWT token upon successful authentication")
public record AuthResponse(
        @Schema(description = "Signed JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
        String accessToken,

        @Schema(description = "Token type scheme", example = "Bearer", requiredMode = Schema.RequiredMode.REQUIRED)
        String tokenType,

        @Schema(description = "Token expiration time in milliseconds", example = "3600000", requiredMode = Schema.RequiredMode.REQUIRED)
        long expiresInMs
) {}
