package com.osvaldevops.login.domain.service;

import com.osvaldevops.login.domain.exception.UserAlreadyExistsException;
import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.CreateUserUseCase;
import com.osvaldevops.login.domain.port.out.UserRepositoryPort;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserDomainService implements CreateUserUseCase {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUser(CreateUserCommand command) {
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("User with email " + command.email() + " already exists");
        }

        Instant now = Instant.now();
        String assignedRole = (command.role() == null || command.role().isBlank())
                ? DEFAULT_ROLE
                : command.role();

        User newUser = User.builder()
                .email(command.email())
                .password(passwordEncoder.encode(command.rawPassword()))
                .userName(command.userName())
                .role(assignedRole)
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return userRepositoryPort.save(newUser);
    }
}

