package com.osvaldevops.login.domain.service;

import com.osvaldevops.login.domain.event.UserCreatedEvent;
import com.osvaldevops.login.domain.exception.UserAlreadyExistsException;
import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.CreateUserUseCase;
import com.osvaldevops.login.domain.port.out.UserEventPublisherPort;
import com.osvaldevops.login.domain.port.out.UserRepositoryPort;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Domain service implementing {@link CreateUserUseCase} to register new user accounts
 * with BCrypt-hashed passwords and publish {@link UserCreatedEvent} messages to Kafka.
 */
@Service
@RequiredArgsConstructor
public class CreateUserDomainService implements CreateUserUseCase {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisherPort userEventPublisherPort;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public User createUser(CreateUserCommand command) {
        // Verify that the email address is not already registered
        if (userRepositoryPort.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("User with email " + command.email() + " already exists");
        }

        Instant now = Instant.now();
        // Default to ROLE_USER when no role is explicitly provided
        String assignedRole = (command.role() == null || command.role().isBlank())
                ? DEFAULT_ROLE
                : command.role();

        // Build domain user with BCrypt-encoded password
        User newUser = User.builder()
                .email(command.email())
                .password(passwordEncoder.encode(command.rawPassword()))
                .userName(command.userName())
                .role(assignedRole)
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepositoryPort.save(newUser);

        // Publish UserCreatedEvent for downstream consumers (e.g., Notification Service)
        UserCreatedEvent event = new UserCreatedEvent(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUserName(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );
        userEventPublisherPort.publishUserCreated(event);

        return savedUser;
    }
}
