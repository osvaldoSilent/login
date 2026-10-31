package com.osvaldevops.login.infrastructure.adapter.in.web.mapper;

import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.AuthResult;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.LoginCommand;
import com.osvaldevops.login.domain.port.in.CreateUserUseCase.CreateUserCommand;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.AuthResponse;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.LoginRequest;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between inbound web DTOs and domain use case commands/results.
 */
@Mapper(componentModel = "spring")
public interface AuthWebMapper {

    /**
     * Maps a {@link LoginRequest} DTO to a domain {@link LoginCommand}.
     */
    @Mapping(source = "password", target = "rawPassword")
    LoginCommand toCommand(LoginRequest request);

    /**
     * Maps a domain {@link AuthResult} to an {@link AuthResponse} DTO.
     */
    AuthResponse toResponse(AuthResult result);

    /**
     * Maps a {@link CreateUserRequest} DTO to a domain {@link CreateUserCommand}.
     */
    @Mapping(source = "password", target = "rawPassword")
    CreateUserCommand toCreateCommand(CreateUserRequest request);

    /**
     * Maps a domain {@link User} model to a {@link UserResponse} DTO.
     */
    UserResponse toUserResponse(User user);
}
