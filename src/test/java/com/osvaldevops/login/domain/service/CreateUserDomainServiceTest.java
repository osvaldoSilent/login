package com.osvaldevops.login.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.osvaldevops.login.domain.event.UserCreatedEvent;
import com.osvaldevops.login.domain.exception.UserAlreadyExistsException;
import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.CreateUserUseCase.CreateUserCommand;
import com.osvaldevops.login.domain.port.out.UserEventPublisherPort;
import com.osvaldevops.login.domain.port.out.UserRepositoryPort;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CreateUserDomainServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEventPublisherPort userEventPublisherPort;

    @InjectMocks
    private CreateUserDomainService createUserDomainService;

    @Test
    @DisplayName("Should create user, save to DB, and publish UserCreatedEvent when email does not exist")
    void shouldCreateUserAndPublishEvent_whenEmailDoesNotExist() {
        CreateUserCommand command = new CreateUserCommand(
                "newuser@osvaldevops.com",
                "rawSecret123",
                "osval_new",
                null
        );

        UUID generatedId = UUID.randomUUID();
        when(userRepositoryPort.existsByEmail(command.email())).thenReturn(false);
        when(passwordEncoder.encode("rawSecret123")).thenReturn("$2a$10$encodedSecret");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> {
            User arg = invocation.getArgument(0);
            return User.builder()
                    .id(generatedId)
                    .email(arg.getEmail())
                    .password(arg.getPassword())
                    .userName(arg.getUserName())
                    .role(arg.getRole())
                    .enabled(arg.isEnabled())
                    .createdAt(arg.getCreatedAt())
                    .updatedAt(arg.getUpdatedAt())
                    .build();
        });

        User created = createUserDomainService.createUser(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser.getEmail()).isEqualTo("newuser@osvaldevops.com");
        assertThat(capturedUser.getPassword()).isEqualTo("$2a$10$encodedSecret");
        assertThat(capturedUser.getUserName()).isEqualTo("osval_new");
        assertThat(capturedUser.getRole()).isEqualTo("ROLE_USER");
        assertThat(capturedUser.isEnabled()).isTrue();
        assertThat(capturedUser.getCreatedAt()).isNotNull();

        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(userEventPublisherPort).publishUserCreated(eventCaptor.capture());
        UserCreatedEvent publishedEvent = eventCaptor.getValue();

        assertThat(publishedEvent.userId()).isEqualTo(generatedId);
        assertThat(publishedEvent.email()).isEqualTo("newuser@osvaldevops.com");
        assertThat(publishedEvent.userName()).isEqualTo("osval_new");
        assertThat(publishedEvent.role()).isEqualTo("ROLE_USER");
        assertThat(publishedEvent.createdAt()).isEqualTo(created.getCreatedAt());
    }

    @Test
    @DisplayName("Should create user with custom role and publish event when role is provided")
    void shouldCreateUserWithCustomRole_whenRoleIsProvided() {
        CreateUserCommand command = new CreateUserCommand(
                "admin@osvaldevops.com",
                "adminSecret123",
                "osval_admin",
                "ROLE_ADMIN"
        );

        when(userRepositoryPort.existsByEmail(command.email())).thenReturn(false);
        when(passwordEncoder.encode("adminSecret123")).thenReturn("$2a$10$encodedAdmin");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = createUserDomainService.createUser(command);

        assertThat(created.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(created.getUserName()).isEqualTo("osval_admin");
        assertThat(created.isEnabled()).isTrue();
        verify(userEventPublisherPort).publishUserCreated(any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should default to ROLE_USER when role is blank")
    void shouldDefaultToRoleUser_whenRoleIsBlank() {
        CreateUserCommand command = new CreateUserCommand(
                "blankrole@osvaldevops.com",
                "secret123",
                "osval_blank",
                "   "
        );

        when(userRepositoryPort.existsByEmail(command.email())).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$encoded");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = createUserDomainService.createUser(command);

        assertThat(created.getRole()).isEqualTo("ROLE_USER");
        verify(userEventPublisherPort).publishUserCreated(any(UserCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException and not publish event when email is already registered")
    void shouldThrowException_whenEmailAlreadyExists() {
        CreateUserCommand command = new CreateUserCommand(
                "existing@osvaldevops.com",
                "rawSecret123",
                "osval",
                "ROLE_USER"
        );

        when(userRepositoryPort.existsByEmail(command.email())).thenReturn(true);

        assertThatThrownBy(() -> createUserDomainService.createUser(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User with email existing@osvaldevops.com already exists");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepositoryPort, never()).save(any());
        verify(userEventPublisherPort, never()).publishUserCreated(any());
    }
}
