package com.osvaldevops.login.infrastructure.adapter.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for {@link UserPersistenceMapper}.
 */
class UserPersistenceMapperTest {

    private final UserPersistenceMapper mapper = Mappers.getMapper(UserPersistenceMapper.class);

    @Test
    @DisplayName("Should map UserJpaEntity to domain User")
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        UserJpaEntity entity = UserJpaEntity.builder()
                .id(id)
                .email("user@osvaldevops.com")
                .password("hashed")
                .userName("osval")
                .role("ROLE_USER")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getEmail()).isEqualTo("user@osvaldevops.com");
        assertThat(domain.getPassword()).isEqualTo("hashed");
        assertThat(domain.getUserName()).isEqualTo("osval");
        assertThat(domain.getRole()).isEqualTo("ROLE_USER");
        assertThat(domain.isEnabled()).isTrue();
        assertThat(domain.getCreatedAt()).isEqualTo(now);
        assertThat(domain.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should map domain User to UserJpaEntity")
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        User domain = User.builder()
                .id(id)
                .email("user@osvaldevops.com")
                .password("hashed")
                .userName("osval")
                .role("ROLE_USER")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserJpaEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getEmail()).isEqualTo("user@osvaldevops.com");
        assertThat(entity.getPassword()).isEqualTo("hashed");
        assertThat(entity.getUserName()).isEqualTo("osval");
        assertThat(entity.getRole()).isEqualTo("ROLE_USER");
        assertThat(entity.isEnabled()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }
}

