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

@Mapper(componentModel = "spring")
public interface AuthWebMapper {

    @Mapping(source = "password", target = "rawPassword")
    LoginCommand toCommand(LoginRequest request);

    AuthResponse toResponse(AuthResult result);

    @Mapping(source = "password", target = "rawPassword")
    CreateUserCommand toCreateCommand(CreateUserRequest request);

    UserResponse toUserResponse(User user);
}
