package com.osvaldevops.login.domain.port.out;

import com.osvaldevops.login.domain.model.User;

/**
 * Outbound port for generating and configuring authentication tokens.
 */
public interface TokenProviderPort {

    /**
     * Generates a signed JWT access token for the given authenticated user.
     *
     * @param user authenticated domain user
     * @return signed JWT token string
     */
    String generateToken(User user);

    /**
     * Returns the configured token expiration duration in milliseconds.
     *
     * @return expiration time in milliseconds
     */
    long getExpirationMs();
}
