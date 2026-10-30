package com.osvaldevops.login.domain.service;

import com.osvaldevops.login.domain.exception.InvalidCredentialsException;
import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase;
import com.osvaldevops.login.domain.port.out.TokenProviderPort;
import com.osvaldevops.login.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationDomainService implements AuthenticateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final TokenProviderPort tokenProviderPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResult authenticate(LoginCommand command) {
        User user = userRepositoryPort.findByEmail(command.email())
                .filter(User::isEnabled)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(command.rawPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = tokenProviderPort.generateToken(user);
        return new AuthResult(token, "Bearer", tokenProviderPort.getExpirationMs());
    }
}
