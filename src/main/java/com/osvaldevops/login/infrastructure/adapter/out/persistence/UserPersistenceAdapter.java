package com.osvaldevops.login.infrastructure.adapter.out.persistence;

import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.out.UserRepositoryPort;
import com.osvaldevops.login.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.osvaldevops.login.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.osvaldevops.login.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = springDataUserRepository.save(userPersistenceMapper.toEntity(user));
        return userPersistenceMapper.toDomain(saved);
    }
}

