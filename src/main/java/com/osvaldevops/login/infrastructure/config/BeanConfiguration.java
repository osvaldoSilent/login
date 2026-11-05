package com.osvaldevops.login.infrastructure.config;

import com.osvaldevops.login.domain.port.in.AuthenticateUserUseCase;
import com.osvaldevops.login.domain.port.in.CreateUserUseCase;
import com.osvaldevops.login.domain.port.out.PasswordEncoderPort;
import com.osvaldevops.login.domain.port.out.TokenProviderPort;
import com.osvaldevops.login.domain.port.out.UserEventPublisherPort;
import com.osvaldevops.login.domain.port.out.UserRepositoryPort;
import com.osvaldevops.login.domain.service.AuthenticationDomainService;
import com.osvaldevops.login.domain.service.CreateUserDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration wiring pure domain services with outbound infrastructure ports.
 */
@Configuration
public class BeanConfiguration {

    /**
     * Registers {@link AuthenticationDomainService} as the implementation of
     * {@link AuthenticateUserUseCase} while keeping the domain package framework-free.
     */
    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(
            UserRepositoryPort userRepositoryPort,
            TokenProviderPort tokenProviderPort,
            PasswordEncoderPort passwordEncoderPort
    ) {
        return new AuthenticationDomainService(userRepositoryPort, tokenProviderPort, passwordEncoderPort);
    }

    /**
     * Registers {@link CreateUserDomainService} as the implementation of
     * {@link CreateUserUseCase} while keeping the domain package framework-free.
     */
    @Bean
    public CreateUserUseCase createUserUseCase(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort,
            UserEventPublisherPort userEventPublisherPort
    ) {
        return new CreateUserDomainService(userRepositoryPort, passwordEncoderPort, userEventPublisherPort);
    }
}

