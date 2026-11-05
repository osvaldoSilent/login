package com.osvaldevops.login.domain.port.out;

/**
 * Outbound port for hashing and verifying user passwords without coupling the domain
 * layer to a specific cryptography framework (e.g., Spring Security BCrypt).
 */
public interface PasswordEncoderPort {

    /**
     * Encodes a raw plain-text password into a secure one-way hash.
     *
     * @param rawPassword the plain-text password to hash
     * @return the encoded password hash
     */
    String encode(String rawPassword);

    /**
     * Verifies whether a raw plain-text password matches a stored encoded password hash.
     *
     * @param rawPassword     the plain-text password provided during authentication
     * @param encodedPassword the stored password hash
     * @return {@code true} if the passwords match, {@code false} otherwise
     */
    boolean matches(String rawPassword, String encodedPassword);
}

