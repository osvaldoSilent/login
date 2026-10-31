package com.osvaldevops.login.domain.port.in;

import com.osvaldevops.login.domain.model.User;

public interface CreateUserUseCase {

    User createUser(CreateUserCommand command);

    record CreateUserCommand(
            String email,
            String rawPassword,
            String userName,
            String role
    ) {}
}

