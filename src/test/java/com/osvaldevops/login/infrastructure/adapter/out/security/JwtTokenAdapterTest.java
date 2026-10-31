package com.osvaldevops.login.infrastructure.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.osvaldevops.login.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenAdapterTest {

    private static final String SECRET_KEY_BASE64 =
            "bG9naW4tc2VydmljZS1zdXBlci1zZWNyZXQta2V5LWZvci1obWFjLXNoYTI1Ni1hdXRoZW50aWNhdGlvbg==";
    private static final long EXPIRATION_MS = 3600000L;

    private JwtTokenAdapter jwtTokenAdapter;
    private SecretKey verificationKey;

    @BeforeEach
    void setUp() {
        jwtTokenAdapter = new JwtTokenAdapter(SECRET_KEY_BASE64, EXPIRATION_MS);
        verificationKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY_BASE64));
    }

    @Test
    @DisplayName("Should generate a valid signed JWT containing subject, userId, and role claims")
    void shouldGenerateValidSignedJwtToken() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("user@osvaldevops.com")
                .password("hashedPassword")
                .userName("osval")
                .role("ROLE_ADMIN")
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        String token = jwtTokenAdapter.generateToken(user);

        assertThat(token).isNotBlank();

        Claims claims = Jwts.parser()
                .verifyWith(verificationKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("user@osvaldevops.com");
        assertThat(claims.get("userId", String.class)).isEqualTo(userId.toString());
        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_ADMIN");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    @DisplayName("Should return configured token expiration time in milliseconds")
    void shouldReturnConfiguredExpirationMs() {
        assertThat(jwtTokenAdapter.getExpirationMs()).isEqualTo(EXPIRATION_MS);
    }
}

