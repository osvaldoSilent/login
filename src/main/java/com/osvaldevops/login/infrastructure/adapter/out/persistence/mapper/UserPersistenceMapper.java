package com.osvaldevops.login.infrastructure.adapter.out.persistence.mapper;

import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {

    User toDomain(UserJpaEntity entity);

    UserJpaEntity toEntity(User domain);
}

