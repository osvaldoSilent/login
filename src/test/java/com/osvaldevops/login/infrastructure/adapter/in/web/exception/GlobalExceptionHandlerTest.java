package com.osvaldevops.login.infrastructure.adapter.in.web.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.osvaldevops.login.domain.exception.EventPublicationException;
import com.osvaldevops.login.domain.exception.InvalidCredentialsException;
import com.osvaldevops.login.domain.exception.UserAccountDisabledException;
import com.osvaldevops.login.domain.exception.UserAlreadyExistsException;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
    }

    @Test
    @DisplayName("Should return 401 UNAUTHORIZED for InvalidCredentialsException")
    void shouldHandleInvalidCredentialsException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid email or password");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleInvalidCredentials(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        ApiErrorResponse body = Objects.requireNonNull(response.getBody());
        assertThat(body.status()).isEqualTo(401);
        assertThat(body.message()).isEqualTo("Invalid email or password");
        assertThat(body.path()).isEqualTo("/api/v1/auth/login");
    }

    @Test
    @DisplayName("Should return 403 FORBIDDEN for UserAccountDisabledException")
    void shouldHandleUserAccountDisabledException() {
        UserAccountDisabledException ex = new UserAccountDisabledException("User account is disabled");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleUserAccountDisabled(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        ApiErrorResponse body = Objects.requireNonNull(response.getBody());
        assertThat(body.status()).isEqualTo(403);
        assertThat(body.message()).isEqualTo("User account is disabled");
    }

    @Test
    @DisplayName("Should return 409 CONFLICT for UserAlreadyExistsException")
    void shouldHandleUserAlreadyExistsException() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleUserAlreadyExists(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ApiErrorResponse body = Objects.requireNonNull(response.getBody());
        assertThat(body.status()).isEqualTo(409);
        assertThat(body.message()).isEqualTo("User already exists");
    }

    @Test
    @DisplayName("Should return 400 BAD_REQUEST with field errors for MethodArgumentNotValidException")
    void shouldHandleValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("loginRequest", "email", "Email must be valid");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleValidationErrors(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiErrorResponse body = Objects.requireNonNull(response.getBody());
        assertThat(body.validationErrors()).containsEntry("email", "Email must be valid");
    }

    @Test
    @DisplayName("Should return 400 BAD_REQUEST for HttpMessageNotReadableException")
    void shouldHandleMalformedJson() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleMalformedJson(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiErrorResponse body = Objects.requireNonNull(response.getBody());
        assertThat(body.message()).isEqualTo("Malformed JSON request body");
    }

    @Test
    @DisplayName("Should return 409 CONFLICT for DataIntegrityViolationException")
    void shouldHandleDataIntegrityViolation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Duplicate key");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).status()).isEqualTo(409);
    }

    @Test
    @DisplayName("Should return 503 SERVICE_UNAVAILABLE for DataAccessException")
    void shouldHandleDatabaseAccessException() {
        QueryTimeoutException ex = new QueryTimeoutException("DB timeout");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleDatabaseAccessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        ApiErrorResponse body = Objects.requireNonNull(response.getBody());
        assertThat(body.status()).isEqualTo(503);
    }

    @Test
    @DisplayName("Should return 503 SERVICE_UNAVAILABLE for EventPublicationException")
    void shouldHandleMessagingException() {
        EventPublicationException ex = new EventPublicationException("Kafka down", new RuntimeException());

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleMessagingException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        ApiErrorResponse body = Objects.requireNonNull(response.getBody());
        assertThat(body.status()).isEqualTo(503);
    }

    @Test
    @DisplayName("Should return 500 INTERNAL_SERVER_ERROR for unexpected Exception")
    void shouldHandleUnexpectedException() {
        Exception ex = new RuntimeException("Unexpected failure");

        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleUnexpectedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiErrorResponse body = Objects.requireNonNull(response.getBody());
        assertThat(body.status()).isEqualTo(500);
    }
}

