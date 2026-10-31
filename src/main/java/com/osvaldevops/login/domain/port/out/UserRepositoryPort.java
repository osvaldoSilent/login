package com.osvaldevops.login.domain.port.out;

import com.osvaldevops.login.domain.model.User;
import java.util.Optional;

/**
 * Outbound port for user persistence operations.
 */
public interface UserRepositoryPort {

    /**
     * Finds a registered user by their unique email address.
     *
     * @param email email address to look up
     * @return an {@link Optional} containing the matching {@link User}, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the specified email address already exists.
     *
     * @param email email address to check
     * @return {@code true} if the email is already registered, {@code false} otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Persists a new or updated domain user.
     *
     * @param user domain user to persist
     * @return the persisted domain {@link User} with generated identifiers
     */
    User save(User user);
}
