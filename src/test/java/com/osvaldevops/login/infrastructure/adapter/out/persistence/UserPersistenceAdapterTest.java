package com.osvaldevops.login.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.osvaldevops.login.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.osvaldevops.login.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock
    private SpringDataUserRepository springDataUserRepository;

    @Mock
    private UserPersistenceMapper userPersistenceMapper;

    @InjectMocks
    private UserPersistenceAdapter userPersistenceAdapter;

    private UUID userId;
    private User domainUser;
    private UserJpaEntity jpaEntity;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        Instant now = Instant.now();

        domainUser = User.builder()
                .id(userId)
                .email("user@osvaldevops.com")
                .password("hashedPassword")
                .userName("osval")
                .role("ROLE_USER")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        jpaEntity = UserJpaEntity.builder()
                .id(userId)
                .email("user@osvaldevops.com")
                .password("hashedPassword")
                .userName("osval")
                .role("ROLE_USER")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    @DisplayName("Should return mapped domain User when email exists in repository")
    void shouldReturnUser_whenFoundByEmail() {
        when(springDataUserRepository.findByEmail("user@osvaldevops.com")).thenReturn(Optional.of(jpaEntity));
        when(userPersistenceMapper.toDomain(jpaEntity)).thenReturn(domainUser);

        Optional<User> result = userPersistenceAdapter.findByEmail("user@osvaldevops.com");

        assertThat(result).isPresent().contains(domainUser);
        verify(springDataUserRepository).findByEmail("user@osvaldevops.com");
        verify(userPersistenceMapper).toDomain(jpaEntity);
    }

    @Test
    @DisplayName("Should return empty Optional when email does not exist in repository")
    void shouldReturnEmpty_whenNotFoundByEmail() {
        when(springDataUserRepository.findByEmail("missing@osvaldevops.com")).thenReturn(Optional.empty());

        Optional<User> result = userPersistenceAdapter.findByEmail("missing@osvaldevops.com");

        assertThat(result).isEmpty();
        verify(springDataUserRepository).findByEmail("missing@osvaldevops.com");
    }

    @Test
    @DisplayName("Should return true when email exists in repository")
    void shouldReturnTrue_whenEmailExists() {
        when(springDataUserRepository.existsByEmail("user@osvaldevops.com")).thenReturn(true);

        boolean exists = userPersistenceAdapter.existsByEmail("user@osvaldevops.com");

        assertThat(exists).isTrue();
        verify(springDataUserRepository).existsByEmail("user@osvaldevops.com");
    }

    @Test
    @DisplayName("Should map domain User to JPA entity, save it, and return mapped domain User")
    void shouldSaveAndReturnDomainUser() {
        when(userPersistenceMapper.toEntity(domainUser)).thenReturn(jpaEntity);
        when(springDataUserRepository.save(jpaEntity)).thenReturn(jpaEntity);
        when(userPersistenceMapper.toDomain(jpaEntity)).thenReturn(domainUser);

        User saved = userPersistenceAdapter.save(domainUser);

        assertThat(saved).isEqualTo(domainUser);
        verify(userPersistenceMapper).toEntity(domainUser);
        verify(springDataUserRepository).save(jpaEntity);
        verify(userPersistenceMapper).toDomain(jpaEntity);
    }
}
