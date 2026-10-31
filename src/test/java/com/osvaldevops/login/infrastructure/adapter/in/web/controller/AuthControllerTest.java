package com.osvaldevops.login.infrastructure.adapter.in.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.AuthResult;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.LoginCommand;
import com.osvaldevops.login.domain.port.in.CreateUserUseCase;
import com.osvaldevops.login.domain.port.in.CreateUserUseCase.CreateUserCommand;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.AuthResponse;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.LoginRequest;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.UserResponse;
import com.osvaldevops.login.infrastructure.adapter.in.web.mapper.AuthWebMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Mock
    private CreateUserUseCase createUserUseCase;

    @Mock
    private AuthWebMapper authWebMapper;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("Should return 200 OK with AuthResponse when login succeeds")
    void shouldReturnOkWithAuthResponse_whenLoginSucceeds() {
        LoginRequest request = new LoginRequest("user@osvaldevops.com", "secret123");
        LoginCommand command = new LoginCommand("user@osvaldevops.com", "secret123");
        AuthResult authResult = new AuthResult("jwt.token.here", "Bearer", 3600000L);
        AuthResponse expectedResponse = new AuthResponse("jwt.token.here", "Bearer", 3600000L);

        when(authWebMapper.toCommand(request)).thenReturn(command);
        when(authenticateUserUseCase.authenticate(command)).thenReturn(authResult);
        when(authWebMapper.toResponse(authResult)).thenReturn(expectedResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);

        verify(authWebMapper).toCommand(request);
        verify(authenticateUserUseCase).authenticate(command);
        verify(authWebMapper).toResponse(authResult);
    }

    @Test
    @DisplayName("Should return 201 CREATED with UserResponse when user registration succeeds")
    void shouldReturnCreatedWithUserResponse_whenRegisterSucceeds() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        CreateUserRequest request = new CreateUserRequest(
                "newuser@osvaldevops.com",
                "secret123",
                "osval",
                "ROLE_USER"
        );
        CreateUserCommand command = new CreateUserCommand(
                "newuser@osvaldevops.com",
                "secret123",
                "osval",
                "ROLE_USER"
        );
        User createdUser = User.builder()
                .id(userId)
                .email("newuser@osvaldevops.com")
                .password("$2a$10$encoded")
                .userName("osval")
                .role("ROLE_USER")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        UserResponse expectedResponse = new UserResponse(
                userId,
                "newuser@osvaldevops.com",
                "osval",
                "ROLE_USER",
                true,
                now
        );

        when(authWebMapper.toCreateCommand(request)).thenReturn(command);
        when(createUserUseCase.createUser(command)).thenReturn(createdUser);
        when(authWebMapper.toUserResponse(createdUser)).thenReturn(expectedResponse);

        ResponseEntity<UserResponse> response = authController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);

        verify(authWebMapper).toCreateCommand(request);
        verify(createUserUseCase).createUser(command);
        verify(authWebMapper).toUserResponse(createdUser);
    }
}
