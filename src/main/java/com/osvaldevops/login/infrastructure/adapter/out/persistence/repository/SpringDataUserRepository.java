package com.osvaldevops.login.infrastructure.adapter.out.persistence.repository;

import com.osvaldevops.login.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link UserJpaEntity} persistence operations.
 */
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    /**
     * Retrieves a user entity by its unique email address.
     */
    Optional<UserJpaEntity> findByEmail(String email);

    /**
     * Checks if a user entity exists with the given email address.
     */
    boolean existsByEmail(String email);
}
