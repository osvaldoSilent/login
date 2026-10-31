package com.osvaldevops.login.infrastructure.adapter.in.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.AuthResult;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.LoginCommand;
import com.osvaldevops.login.domain.port.in.CreateUserUseCase.CreateUserCommand;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.AuthResponse;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.LoginRequest;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.UserResponse;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AuthWebMapperTest {

    private final AuthWebMapper mapper = Mappers.getMapper(AuthWebMapper.class);

    @Test
    @DisplayName("Should map LoginRequest to LoginCommand")
    void shouldMapLoginRequestToLoginCommand() {
        LoginRequest request = new LoginRequest("user@osvaldevops.com", "secret123");

        LoginCommand command = mapper.toCommand(request);

        assertThat(command).isNotNull();
        assertThat(command.email()).isEqualTo("user@osvaldevops.com");
        assertThat(command.rawPassword()).isEqualTo("secret123");
    }

    @Test
    @DisplayName("Should map AuthResult to AuthResponse")
    void shouldMapAuthResultToAuthResponse() {
        AuthResult result = new AuthResult("jwt-token", "Bearer", 3600000L);

        AuthResponse response = mapper.toResponse(result);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInMs()).isEqualTo(3600000L);
    }

    @Test
    @DisplayName("Should map CreateUserRequest to CreateUserCommand")
    void shouldMapCreateUserRequestToCreateUserCommand() {
        CreateUserRequest request = new CreateUserRequest(
                "newuser@osvaldevops.com",
                "secret123",
                "osval",
                "ROLE_ADMIN"
        );

        CreateUserCommand command = mapper.toCreateCommand(request);

        assertThat(command).isNotNull();
        assertThat(command.email()).isEqualTo("newuser@osvaldevops.com");
        assertThat(command.rawPassword()).isEqualTo("secret123");
        assertThat(command.userName()).isEqualTo("osval");
        assertThat(command.role()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should map domain User to UserResponse")
    void shouldMapUserToUserResponse() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        User user = User.builder()
                .id(id)
                .email("user@osvaldevops.com")
                .password("hashed")
                .userName("osval")
                .role("ROLE_USER")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserResponse response = mapper.toUserResponse(user);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.email()).isEqualTo("user@osvaldevops.com");
        assertThat(response.userName()).isEqualTo("osval");
        assertThat(response.role()).isEqualTo("ROLE_USER");
        assertThat(response.enabled()).isTrue();
        assertThat(response.createdAt()).isEqualTo(now);
    }
}

