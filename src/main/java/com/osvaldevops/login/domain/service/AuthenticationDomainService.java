package com.osvaldevops.login.domain.service;

import com.osvaldevops.login.domain.exception.InvalidCredentialsException;
import com.osvaldevops.login.domain.exception.UserAccountDisabledException;
import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase;
import com.osvaldevops.login.domain.port.out.PasswordEncoderPort;
import com.osvaldevops.login.domain.port.out.TokenProviderPort;
import com.osvaldevops.login.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

/**
 * Pure domain service implementing {@link AuthenticateUserUseCase} to validate user credentials
 * and issue signed Bearer JWT tokens through outbound ports.
 */
@RequiredArgsConstructor
public class AuthenticationDomainService implements AuthenticateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final TokenProviderPort tokenProviderPort;
    private final PasswordEncoderPort passwordEncoderPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthResult authenticate(LoginCommand command) {
        // Retrieve user by email or reject with 401 Unauthorized
        User user = userRepositoryPort.findByEmail(command.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Ensure the user account is active before verifying password
        if (!user.isEnabled()) {
            throw new UserAccountDisabledException("User account is disabled");
        }

        // Verify raw password against stored hash via outbound port
        if (!passwordEncoderPort.matches(command.rawPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate signed JWT token and return authentication result
        String token = tokenProviderPort.generateToken(user);
        return new AuthResult(token, "Bearer", tokenProviderPort.getExpirationMs());
    }
}
