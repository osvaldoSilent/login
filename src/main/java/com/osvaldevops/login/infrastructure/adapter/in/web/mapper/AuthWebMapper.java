package com.osvaldevops.login.infrastructure.adapter.in.web.mapper;

import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.AuthResult;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.LoginCommand;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.AuthResponse;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.LoginRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthWebMapper {

    @Mapping(source = "password", target = "rawPassword")
    LoginCommand toCommand(LoginRequest request);

    AuthResponse toResponse(AuthResult result);
}

