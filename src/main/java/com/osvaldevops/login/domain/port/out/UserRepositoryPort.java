package com.osvaldevops.login.domain.port.out;

import com.osvaldevops.login.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByEmail(String email);

    User save(User user);
}

