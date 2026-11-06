# Login & User Registration Service (`login-service`)

Servicio Cloud-Native de **Autenticación (JWT) y Registro de Usuarios** construido con **Java 25**, **Spring Boot 3.4.5**, **Arquitectura Hexagonal Pura (Ports & Adapters)**, **Spring Security 6 (Stateless + BCrypt)**, **Spring Data JPA + Flyway**, **PostgreSQL (Neon Serverless / Docker)** y **Google Cloud Pub/Sub**, optimizado para bajo consumo de memoria en **Google Cloud Run**, documentado con **OpenAPI 3 / Swagger UI**, y asegurado con **JaCoCo (100% cobertura)** y **SonarCloud**.

---

## 1. Stack Tecnológico

| Categoría | Tecnología | Versión / Detalle |
| :--- | :--- | :--- |
| **Lenguaje & Runtime** | Java (Eclipse Temurin) | **25** (Virtual Threads + Compact Object Headers `-XX:+UseCompactObjectHeaders`) |
| **Framework Base** | Spring Boot | **3.4.5** (`lazy-initialization: true`, `open-in-view: false`) |
| **Seguridad & Auth** | Spring Security 6 + JJWT | Stateless Filter Chain + `BCryptPasswordEncoder` + `io.jsonwebtoken:jjwt` `0.12.6` (`HMAC-SHA256`) |
| **Persistencia** | Spring Data JPA + Flyway + PostgreSQL | Neon Serverless PostgreSQL + HikariCP (`minimum-idle: 0`, `maximum-pool-size: 5`) |
| **Mensajería Asíncrona** | Spring Cloud GCP Pub/Sub | `spring-cloud-gcp-starter-pubsub` (`5.9.0`) |
| **Mapeo & Boilerplate** | MapStruct + Lombok | MapStruct `1.6.3` (`componentModel = "spring"`) |
| **Documentación API** | SpringDoc OpenAPI 3 | `springdoc-openapi-starter-webmvc-ui` `2.8.6` (`/swagger-ui.html`, `/v3/api-docs`) |
| **Calidad & Testing** | JUnit 5, Mockito, AssertJ, Cucumber, JaCoCo, SonarQube | **39 unit tests** (100% cobertura JaCoCo en las 15 clases analizadas) + SonarCloud Quality Gate |
| **Contenedor & CI/CD** | Jib + GitHub Actions + Cloud Run | `eclipse-temurin:25-jre-alpine` + Workload Identity Federation (OIDC sin llaves JSON) |

---

## 2. Arquitectura Hexagonal Pura (`com.osvaldevops.login`)

El paquete `domain` es **100% Java puro** (cero dependencias de `org.springframework.*`, Jakarta JPA o librerías de infraestructura). El ensamblaje de los servicios de dominio con los puertos de salida se realiza exclusivamente en [`BeanConfiguration`](src/main/java/com/osvaldevops/login/infrastructure/config/BeanConfiguration.java).

```text
login-service/
├── .antigravity/
│   ├── context-synthesizer.md
│   └── security-and-memory-checklist.md
├── .github/workflows/
│   └── ci-cd.yml
├── compose.yaml
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/osvaldevops/login/
    │   │   ├── LoginServiceApplication.java
    │   │   ├── domain/                                  # 100% Pure Java (Zero Spring imports)
    │   │   │   ├── event/
    │   │   │   │   └── UserCreatedEvent.java
    │   │   │   ├── exception/
    │   │   │   │   ├── EventPublicationException.java
    │   │   │   │   ├── InvalidCredentialsException.java
    │   │   │   │   ├── UserAccountDisabledException.java
    │   │   │   │   └── UserAlreadyExistsException.java
    │   │   │   ├── model/
    │   │   │   │   └── User.java
    │   │   │   ├── port/
    │   │   │   │   ├── in/
    │   │   │   │   │   ├── AuthenticateUserUseCase.java
    │   │   │   │   │   └── CreateUserUseCase.java
    │   │   │   │   └── out/
    │   │   │   │       ├── PasswordEncoderPort.java
    │   │   │   │       ├── TokenProviderPort.java
    │   │   │   │       ├── UserEventPublisherPort.java
    │   │   │   │       └── UserRepositoryPort.java
    │   │   │   └── service/
    │   │   │       ├── AuthenticationDomainService.java
    │   │   │       └── CreateUserDomainService.java
    │   │   └── infrastructure/                          # Frameworks, Drivers & Adapters
    │   │       ├── adapter/
    │   │       │   ├── in/web/
    │   │       │   │   ├── controller/AuthController.java
    │   │       │   │   ├── dto/ (LoginRequest, AuthResponse, CreateUserRequest, UserResponse, ApiErrorResponse)
    │   │       │   │   ├── exception/GlobalExceptionHandler.java
    │   │       │   │   └── mapper/AuthWebMapper.java
    │   │       │   └── out/
    │   │       │       ├── messaging/
    │   │       │       │   └── PubSubUserEventPublisherAdapter.java
    │   │       │       ├── persistence/
    │   │       │       │   ├── UserPersistenceAdapter.java
    │   │       │       │   ├── entity/UserJpaEntity.java
    │   │       │       │   ├── mapper/UserPersistenceMapper.java
    │   │       │       │   └── repository/SpringDataUserRepository.java
    │   │       │       └── security/
    │   │       │           ├── BCryptPasswordEncoderAdapter.java
    │   │       │           └── JwtTokenAdapter.java
    │   │       └── config/
    │   │           ├── BeanConfiguration.java
    │   │           ├── OpenApiConfig.java
    │   │           └── SecurityConfig.java
    │   └── resources/
    │       ├── application.yml                          # Ignorado en Git (desarrollo local)
    │       ├── application-dev.yml                      # Versionado con variables ${ENV_VAR}
    │       └── db/migration/V1__init_users_table.sql
    └── test/
        └── java/com/osvaldevops/login/                  # 39 Unit Tests (100% JaCoCo Coverage)
```

---

## 3. Contrato REST API & OpenAPI 3 (Swagger UI)

Con el servicio en ejecución, la documentación interactiva está disponible en:
- **Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON Spec:** `http://localhost:8081/v3/api-docs`

| Método | Endpoint | Descripción | Respuestas Documentadas |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Autentica usuario por email y contraseña y emite un JWT firmado (`HMAC-SHA256`). | `200 OK` (`AuthResponse`), `400`, `401`, `403`, `500`, `503` (`ApiErrorResponse`) |
| `POST` | `/api/v1/auth/register` | Registra un nuevo usuario con hash BCrypt y publica `UserCreatedEvent` en GCP Pub/Sub. | `201 Created` (`UserResponse`), `400`, `409`, `500`, `503` (`ApiErrorResponse`) |

---

## 4. Integración Event-Driven con Google Cloud Pub/Sub

Cuando se registra un usuario mediante `POST /api/v1/auth/register`:
1. [`CreateUserDomainService`](src/main/java/com/osvaldevops/login/domain/service/CreateUserDomainService.java) verifica unicidad con `existsByEmail`, hashea la contraseña vía [`PasswordEncoderPort`](src/main/java/com/osvaldevops/login/domain/port/out/PasswordEncoderPort.java) y persiste la entidad en PostgreSQL.
2. Emite un [`UserCreatedEvent`](src/main/java/com/osvaldevops/login/domain/event/UserCreatedEvent.java) hacia [`UserEventPublisherPort`](src/main/java/com/osvaldevops/login/domain/port/out/UserEventPublisherPort.java).
3. [`PubSubUserEventPublisherAdapter`](src/main/java/com/osvaldevops/login/infrastructure/adapter/out/messaging/PubSubUserEventPublisherAdapter.java) publica el evento JSON en el topic configurado (`${PUBSUB_USER_CREATED_TOPIC}`) junto con los atributos `eventType=USER_CREATED` y `userId=<UUID>`, para que [`notifications-service`](../notifications-service) lo procese vía suscripción Push en Cloud Run.

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "osval@osvaldevops.com",
  "userName": "osval",
  "role": "ROLE_USER",
  "createdAt": "2026-11-05T19:00:00Z"
}
```

---

## 5. Seguridad, Gestión de Secretos y Optimización de Memoria (Cloud Run)

### Seguridad y Secretos (Zero-Trust)
- **Aislamiento de Configuraciones**: `src/main/resources/application.yml` está ignorado en [`.gitignore`](.gitignore) para pruebas locales; [`src/main/resources/application-dev.yml`](src/main/resources/application-dev.yml) está versionado en Git con todas las credenciales enmascaradas mediante variables de entorno (`${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`, `${GCP_PROJECT_ID}`, `${PUBSUB_USER_CREATED_TOPIC}`, `${JWT_SECRET}`, `${JWT_EXPIRATION_MS}`).
- **Sin Fallbacks Hardcodeados en Java**: Los adaptadores [`JwtTokenAdapter`](src/main/java/com/osvaldevops/login/infrastructure/adapter/out/security/JwtTokenAdapter.java) y [`PubSubUserEventPublisherAdapter`](src/main/java/com/osvaldevops/login/infrastructure/adapter/out/messaging/PubSubUserEventPublisherAdapter.java) exigen explícitamente sus propiedades desde el entorno sin valores por defecto en código.
- **Protección de Logs y Anti-Enumeración**: `spring.jpa.show-sql: false`, `org.springframework.security: INFO`, mensajes genéricos ante credenciales inválidas y validación de cuenta activa (`!user.isEnabled()`) previa a la ejecución de BCrypt.

### Optimización de Memoria y Cold-Start (Java 25 + Cloud Run `512Mi`)
- **Imagen Base Alpine JRE 25**: Construcción sin demonios Docker mediante Jib usando `eclipse-temurin:25-jre-alpine` (~95 MB).
- **Flags JVM para Contenedores**: `-XX:+UseCompactObjectHeaders` (reduce cabeceras de objetos a 64 bits en Java 25), `-XX:+UseSerialGC`, `-XX:MaxRAMPercentage=75.0` y `-XX:TieredStopAtLevel=1`.
- **Dieta Spring Boot & HikariCP Serverless**: `spring.main.lazy-initialization: true`, `spring.threads.virtual.enabled: true`, `spring.jpa.open-in-view: false`, `minimum-idle: 0` y `maximum-pool-size: 5` (permite *scale-to-zero* en Neon PostgreSQL).

---

## 6. Documentación de Agentes y Reglas de Ingeniería (`.antigravity/*.md`)

El proyecto incluye especificaciones y reglas en formato Markdown para estandarizar el desarrollo asistido por IA, revisiones de arquitectura y auditorías de calidad:

### A. Específicos de `login-service` ([`login-service/.antigravity/`](.antigravity))
| Archivo `.md` | Descripción |
| :--- | :--- |
| [`.antigravity/context-synthesizer.md`](.antigravity/context-synthesizer.md) | **Sintetizador de Contexto por Iteración:** Snapshot arquitectónico de alta densidad, mapa de componentes hexagonales, contrato de variables de entorno y bitácora histórica de iteraciones de `login-service` para iniciar sesiones sin escanear todo el repositorio. |
| [`.antigravity/security-and-memory-checklist.md`](.antigravity/security-and-memory-checklist.md) | **Checklist de Seguridad y Memoria:** Lista de verificación pre-despliegue para auditar enmascaramiento de secretos, JWT/BCrypt, pool HikariCP para Neon Serverless y banderas JVM/Jib de ahorro de RAM en Cloud Run. |

### B. Globales del Workspace ([`../.antigravity/`](../.antigravity))
| Archivo `.md` | Descripción |
| :--- | :--- |
| [`../.antigravity/spring_boot_init.md`](../.antigravity/spring_boot_init.md) | **Blueprint de Inicialización Cloud-Native:** Estándares base para microservicios con Java 25, Spring Boot 3.4+, Neon Postgres, GCP Pub/Sub y despliegue serverless. |
| [`../.antigravity/hexagonal-validator.md`](../.antigravity/hexagonal-validator.md) | **Validador de Arquitectura Hexagonal:** Reglas estrictas de pureza del paquete `domain` (cero imports de Spring), puertos de entrada/salida y ensamblaje mediante `BeanConfiguration`. |
| [`../.antigravity/unit-test-agent.md`](../.antigravity/unit-test-agent.md) | **Agente de Pruebas Unitarias:** Guía para tests unitarios con JUnit 5, Mockito, AssertJ y configuración Surefire/ByteBuddy en Java 25 para alcanzar $\ge 80\%$ (100% actual) en JaCoCo. |
| [`../.antigravity/security-and-secrets-auditor.md`](../.antigravity/security-and-secrets-auditor.md) | **Auditor DevSecOps y Secretos:** Prevención de fugas de credenciales, uso de variables `${ENV_VAR}`, GitHub Secrets y Workload Identity Federation (OIDC). |
| [`../.antigravity/gcp-cloudrun-pubsub-architect.md`](../.antigravity/gcp-cloudrun-pubsub-architect.md) | **Arquitecto GCP Cloud Run & Pub/Sub:** Buenas prácticas de publicación/suscripción idempotente en Pub/Sub, Jib y configuración de escalado en Cloud Run. |
| [`../.antigravity/bdd-cucumber-agent.md`](../.antigravity/bdd-cucumber-agent.md) | **Agente BDD con Cucumber:** Lineamientos para escenarios Gherkin (`.feature`) y pruebas de aceptación desacopladas de infraestructura externa. |
| [`../.antigravity/sonarqube-quality-gate.md`](../.antigravity/sonarqube-quality-gate.md) | **Quality Gate de SonarQube/SonarCloud:** Reglas para mantener `0` Bugs, `0` Vulnerabilities, `0` Security Hotspots y código limpio en el pipeline CI/CD. |
| [`../.antigravity/api-contract-and-docs.md`](../.antigravity/api-contract-and-docs.md) | **Estándar de Contratos REST y OpenAPI 3:** Reglas de diseño de DTOs (`record`), manejo centralizado de errores (`ApiErrorResponse`) y documentación Swagger UI. |
| [`../.antigravity/code-generation-behavior.md`](../.antigravity/code-generation-behavior.md) | **Disciplina de Generación de Código (Minimal Diff):** Regla de comportamiento para realizar cambios quirúrgicos enfocados exclusivamente en los archivos requeridos por la lógica solicitada. |

---

## 7. Comandos de Ejecución y Verificación

```powershell
# Ejecutar suite completa de pruebas unitarias + reporte y verificación JaCoCo (fase verify)
mvn verify

# Levantar dependencias locales (PostgreSQL, Emulador Pub/Sub, SonarQube)
docker compose up -d
```
