package com.osvaldevops.login.infrastructure.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for {@link BCryptPasswordEncoderAdapter}.
 */
@ExtendWith(MockitoExtension.class)
class BCryptPasswordEncoderAdapterTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BCryptPasswordEncoderAdapter bCryptPasswordEncoderAdapter;

    @Test
    @DisplayName("Should delegate password encoding to Spring Security PasswordEncoder")
    void should_ReturnEncodedPassword_When_EncodeIsCalled() {
        // Given
        when(passwordEncoder.encode("rawSecret123")).thenReturn("$2a$10$hashedPassword");

        // When
        String encoded = bCryptPasswordEncoderAdapter.encode("rawSecret123");

        // Then
        assertThat(encoded).isEqualTo("$2a$10$hashedPassword");
        verify(passwordEncoder).encode("rawSecret123");
    }

    @Test
    @DisplayName("Should delegate password verification to Spring Security PasswordEncoder")
    void should_ReturnTrue_When_RawPasswordMatchesEncodedPassword() {
        // Given
        when(passwordEncoder.matches("rawSecret123", "$2a$10$hashedPassword")).thenReturn(true);

        // When
        boolean matches = bCryptPasswordEncoderAdapter.matches("rawSecret123", "$2a$10$hashedPassword");

        // Then
        assertThat(matches).isTrue();
        verify(passwordEncoder).matches("rawSecret123", "$2a$10$hashedPassword");
    }
}

