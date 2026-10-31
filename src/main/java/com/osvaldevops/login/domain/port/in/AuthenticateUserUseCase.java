package com.osvaldevops.login.domain.port.in;

/**
 * Inbound port (use case) for authenticating a user and issuing a signed JWT token.
 */
public interface AuthenticateUserUseCase {

    /**
     * Validates user credentials and generates a signed Bearer JWT token.
     *
     * @param command login credentials containing email and plain-text password
     * @return authentication result containing the signed JWT access token and expiration metadata
     */
    AuthResult authenticate(LoginCommand command);

    /**
     * Command payload for user authentication.
     *
     * @param email       registered email address of the user
     * @param rawPassword plain-text password provided by the user
     */
    record LoginCommand(String email, String rawPassword) {}

    /**
     * Result payload returned upon successful user authentication.
     *
     * @param accessToken signed JWT access token
     * @param tokenType   token scheme (e.g., "Bearer")
     * @param expiresInMs token expiration time in milliseconds
     */
    record AuthResult(String accessToken, String tokenType, long expiresInMs) {}
}
