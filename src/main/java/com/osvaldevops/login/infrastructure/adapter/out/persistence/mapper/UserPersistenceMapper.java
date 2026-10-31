package com.osvaldevops.login.infrastructure.adapter.out.persistence.mapper;

import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper converting between {@link UserJpaEntity} database entities and domain {@link User} models.
 */
@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    /**
     * Maps a persistence {@link UserJpaEntity} to a domain {@link User}.
     */
    User toDomain(UserJpaEntity entity);

    /**
     * Maps a domain {@link User} to a persistence {@link UserJpaEntity}.
     */
    UserJpaEntity toEntity(User domain);
}
