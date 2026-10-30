package com.osvaldevops.login.infrastructure.adapter.in.web.controller;

import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase;
import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase.AuthResult;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.AuthResponse;
import com.osvaldevops.login.infrastructure.adapter.in.web.dto.LoginRequest;
import com.osvaldevops.login.infrastructure.adapter.in.web.mapper.AuthWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final AuthWebMapper authWebMapper;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authenticateUserUseCase.authenticate(authWebMapper.toCommand(request));
        return ResponseEntity.ok(authWebMapper.toResponse(result));
    }
}

