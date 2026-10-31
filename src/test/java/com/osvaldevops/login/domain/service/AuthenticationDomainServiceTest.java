package com.osvaldevops.login.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.osvaldevops.login.domain.exception.InvalidCredentialsException;
import com.osvaldevops.login.domain.exception.UserAccountDisabledException;
import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.AuthResult;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.LoginCommand;
import com.osvaldevops.login.domain.port.out.TokenProviderPort;
import com.osvaldevops.login.domain.port.out.UserRepositoryPort;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationDomainServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationDomainService authenticationDomainService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(UUID.randomUUID())
                .email("user@osvaldevops.com")
                .password("$2a$10$hashedPassword")
                .userName("osval")
                .role("ROLE_USER")
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should authenticate user and return JWT token when credentials are valid")
    void shouldAuthenticateSuccessfully_whenCredentialsAreValid() {
        LoginCommand command = new LoginCommand("user@osvaldevops.com", "rawSecret123");

        when(userRepositoryPort.findByEmail(command.email())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(command.rawPassword(), activeUser.getPassword())).thenReturn(true);
        when(tokenProviderPort.generateToken(activeUser)).thenReturn("mocked.jwt.token");
        when(tokenProviderPort.getExpirationMs()).thenReturn(3600000L);

        AuthResult result = authenticationDomainService.authenticate(command);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresInMs()).isEqualTo(3600000L);

        verify(userRepositoryPort).findByEmail("user@osvaldevops.com");
        verify(passwordEncoder).matches("rawSecret123", "$2a$10$hashedPassword");
        verify(tokenProviderPort).generateToken(activeUser);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when email does not exist")
    void shouldThrowException_whenUserNotFound() {
        LoginCommand command = new LoginCommand("unknown@osvaldevops.com", "rawSecret123");

        when(userRepositoryPort.findByEmail(command.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationDomainService.authenticate(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProviderPort, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw UserAccountDisabledException when user account is disabled")
    void shouldThrowException_whenUserIsDisabled() {
        User disabledUser = User.builder()
                .id(UUID.randomUUID())
                .email("disabled@osvaldevops.com")
                .password("$2a$10$hashedPassword")
                .userName("disabled_user")
                .role("ROLE_USER")
                .enabled(false)
                .createdAt(Instant.now())
                .build();

        LoginCommand command = new LoginCommand("disabled@osvaldevops.com", "rawSecret123");
        when(userRepositoryPort.findByEmail(command.email())).thenReturn(Optional.of(disabledUser));

        assertThatThrownBy(() -> authenticationDomainService.authenticate(command))
                .isInstanceOf(UserAccountDisabledException.class)
                .hasMessage("User account is disabled");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProviderPort, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password does not match")
    void shouldThrowException_whenPasswordIsIncorrect() {
        LoginCommand command = new LoginCommand("user@osvaldevops.com", "wrongPassword");

        when(userRepositoryPort.findByEmail(command.email())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(command.rawPassword(), activeUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authenticationDomainService.authenticate(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(tokenProviderPort, never()).generateToken(any());
    }
}
