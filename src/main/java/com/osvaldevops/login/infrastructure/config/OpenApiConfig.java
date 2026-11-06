package com.osvaldevops.login.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger UI configuration defining API metadata, relative server URL,
 * and Bearer JWT security scheme.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Configures the custom {@link OpenAPI} specification for Swagger UI.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Login & User Service API")
                        .version("1.0.0")
                        .description("Hexagonal Architecture Authentication & User Registration Service with Spring Security, JWT (HMAC-SHA256), JPA, PostgreSQL, and Google Cloud Pub/Sub.")
                        .contact(new Contact()
                                .name("OsvalDevOps")
                                .email("contact@osvaldevops.com")))
                .servers(List.of(
                        new Server()
                                .url("/")
                                .description("Default Server (Local / Google Cloud Run)")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide the JWT access token obtained from POST /api/v1/auth/login")));
    }
}
