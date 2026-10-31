package com.osvaldevops.login.domain.port.in;

import com.osvaldevops.login.domain.model.User;

/**
 * Inbound port (use case) for registering a new user account.
 */
public interface CreateUserUseCase {

    /**
     * Registers a new user account, persists it in the repository, and publishes a {@code UserCreatedEvent}.
     *
     * @param command user registration details
     * @return the newly persisted domain {@link User}
     */
    User createUser(CreateUserCommand command);

    /**
     * Command payload for registering a new user account.
     *
     * @param email       unique email address for the new user
     * @param rawPassword plain-text password to be hashed before persistence
     * @param userName    display username
     * @param role        optional authorization role (defaults to {@code ROLE_USER} if omitted or blank)
     */
    record CreateUserCommand(
            String email,
            String rawPassword,
            String userName,
            String role
    ) {}
}
