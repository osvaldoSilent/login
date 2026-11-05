package com.osvaldevops.login.infrastructure.adapter.in.web.controller;

import com.osvaldevops.login.domain.model.User;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.AuthResult;
import com.osvaldevops.login.domain.port.in.CreateUserUseCase;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.ApiErrorResponse;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.AuthResponse;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.LoginRequest;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.UserResponse;
import com.osvaldevops.login.infrastructure.adapter.in.web.mapper.AuthWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound REST controller exposing authentication and user registration endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Users", description = "Endpoints for user login and registration")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final AuthWebMapper authWebMapper;

    /**
     * Authenticates a user by email and password and returns a signed Bearer JWT token.
     *
     * @param request validated login request payload
     * @return {@code 200 OK} with {@link AuthResponse} containing the signed JWT token
     */
    @Operation(
            summary = "Authenticate user and generate JWT",
            description = "Validates user email and password against PostgreSQL and returns a signed Bearer JWT token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or malformed JSON",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User account is disabled",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database unavailable",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authenticateUserUseCase.authenticate(authWebMapper.toCommand(request));
        return ResponseEntity.ok(authWebMapper.toResponse(result));
    }

    /**
     * Registers a new user account in PostgreSQL and publishes a {@code UserCreatedEvent} to Google Cloud Pub/Sub.
     *
     * @param request validated user registration request payload
     * @return {@code 201 Created} with {@link UserResponse} representing the newly created user
     */
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with a BCrypt-hashed password in PostgreSQL and publishes a UserCreatedEvent to Google Cloud Pub/Sub."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or malformed JSON",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User with the given email already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Database or Google Cloud Pub/Sub messaging unavailable",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        User createdUser = createUserUseCase.createUser(authWebMapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(authWebMapper.toUserResponse(createdUser));
    }
}
