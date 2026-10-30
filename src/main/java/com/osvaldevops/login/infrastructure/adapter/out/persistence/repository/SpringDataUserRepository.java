package com.osvaldevops.login.infrastructure.adapter.out.persistence.repository;

import com.osvaldevops.login.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);
}

