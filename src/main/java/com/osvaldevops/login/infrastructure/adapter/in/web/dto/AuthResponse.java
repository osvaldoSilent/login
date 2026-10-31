package com.osvaldevops.login.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload containing the signed JWT token upon successful authentication")
public record AuthResponse(
        @Schema(description = "Signed JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Token type scheme", example = "Bearer")
        String tokenType,

        @Schema(description = "Token expiration time in milliseconds", example = "3600000")
        long expiresInMs
) {}
