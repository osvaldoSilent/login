package com.osvaldevops.login.infrastructure.adapter.out.security;

import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.out.TokenProviderPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Outbound security adapter implementing {@link TokenProviderPort} to generate
 * HMAC-SHA256 signed JWT tokens using JJWT.
 */
@Component
public class JwtTokenAdapter implements TokenProviderPort {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtTokenAdapter(
            @Value("${security.jwt.secret-key}") String secretKeyBase64,
            @Value("${security.jwt.expiration-ms}") long expirationMs
    ) {
        // Decode Base64-encoded secret key for HMAC-SHA signing
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyBase64));
        this.expirationMs = expirationMs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        // Build and sign JWT with user subject, custom claims, and expiration timestamp
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getExpirationMs() {
        return expirationMs;
    }
}
