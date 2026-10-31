package com.osvaldevops.login.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for user authentication")
public record LoginRequest(
        @Schema(description = "Registered email address of the user", example = "osval@osvaldevops.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "Plain-text password of the user", example = "secretPassword123")
        @NotBlank(message = "Password is required")
        String password
) {}
