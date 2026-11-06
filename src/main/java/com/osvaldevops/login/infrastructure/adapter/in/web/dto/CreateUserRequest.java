package com.osvaldevops.login.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for registering a new user account via {@code POST /api/v1/auth/register}.
 */
@Schema(description = "Request payload for registering a new user account")
public record CreateUserRequest(
        @Schema(description = "Unique email address for the new user", example = "osval@osvaldevops.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "Password (minimum 6 characters)", example = "secretPassword123", minLength = 6, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @Schema(description = "Display username", example = "osval", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "User name is required")
        String userName,

        @Schema(description = "Optional user role (defaults to ROLE_USER if omitted)", example = "ROLE_USER", defaultValue = "ROLE_USER", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String role
) {}
