package com.osvaldevops.login.domain.service;

import com.osvaldevops.login.domain.exception.InvalidCredentialsException;
import com.osvaldevops.login.domain.exception.UserAccountDisabledException;
import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase;
import com.osvaldevops.login.domain.port.out.TokenProviderPort;
import com.osvaldevops.login.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Domain service implementing {@link AuthenticateUserUseCase} to validate user credentials
 * against PostgreSQL and issue signed Bearer JWT tokens.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationDomainService implements AuthenticateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final TokenProviderPort tokenProviderPort;
    private final PasswordEncoder passwordEncoder;

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

        // Verify raw password against stored BCrypt hash
        if (!passwordEncoder.matches(command.rawPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate signed JWT token and return authentication result
        String token = tokenProviderPort.generateToken(user);
        return new AuthResult(token, "Bearer", tokenProviderPort.getExpirationMs());
    }
}
