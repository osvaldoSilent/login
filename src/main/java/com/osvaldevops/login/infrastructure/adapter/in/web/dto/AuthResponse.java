package com.osvaldevops.login.infrastructure.adapter.in.web.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMs
) {}

