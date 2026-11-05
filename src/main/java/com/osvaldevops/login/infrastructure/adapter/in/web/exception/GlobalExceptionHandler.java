package com.osvaldevops.login.infrastructure.adapter.in.web.exception;

import com.osvaldevops.login.domain.exception.EventPublicationException;
import com.osvaldevops.login.domain.exception.InvalidCredentialsException;
import com.osvaldevops.login.domain.exception.UserAccountDisabledException;
import com.osvaldevops.login.domain.exception.UserAlreadyExistsException;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized REST exception handler translating domain, validation, persistence,
 * and messaging exceptions into standardized {@link ApiErrorResponse} payloads.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles authentication failures due to invalid email or password ({@code 401 Unauthorized}).
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Handles authentication attempts on disabled user accounts ({@code 403 Forbidden}).
     */
    @ExceptionHandler(UserAccountDisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAccountDisabled(
            UserAccountDisabledException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Handles duplicate user registration attempts ({@code 409 Conflict}).
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
    }

    /**
     * Handles Bean Validation failures on incoming request bodies ({@code 400 Bad Request}).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        // Collect the first validation message per invalid field
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields",
                request.getRequestURI(),
                fieldErrors
        );
    }

    /**
     * Handles unreadable or malformed JSON payloads ({@code 400 Bad Request}).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request body",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Handles database unique or foreign key constraint violations ({@code 409 Conflict}).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Data integrity violation on {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.CONFLICT,
                "Database integrity constraint violated",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Handles database connectivity or query timeout failures ({@code 503 Service Unavailable}).
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabaseAccessException(
            DataAccessException ex,
            HttpServletRequest request
    ) {
        log.error("Database access error on {}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Database service is temporarily unavailable",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Handles Google Cloud Pub/Sub event publication failures ({@code 503 Service Unavailable}).
     */
    @ExceptionHandler(EventPublicationException.class)
    public ResponseEntity<ApiErrorResponse> handleMessagingException(
            EventPublicationException ex,
            HttpServletRequest request
    ) {
        log.error("GCP Pub/Sub messaging error on {}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Messaging service is temporarily unavailable",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Fallback handler for any unhandled runtime or checked exceptions ({@code 500 Internal Server Error}).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Builds a standardized {@link ResponseEntity} wrapping an {@link ApiErrorResponse}.
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                validationErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
