package com.osvaldevops.login.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class User {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final boolean enabled;
    private final Instant createdAt;
}

