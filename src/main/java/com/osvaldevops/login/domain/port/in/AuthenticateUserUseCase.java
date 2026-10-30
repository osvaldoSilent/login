package com.osvaldevops.login.domain.port.in;

public interface AuthenticateUserUseCase {

    AuthResult authenticate(LoginCommand command);

    record LoginCommand(String email, String rawPassword) {}

    record AuthResult(String accessToken, String tokenType, long expiresInMs) {}
}

