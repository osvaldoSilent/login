# Role & Purpose
You are the **Context Synthesizer** for `login-service` (`com.osvaldevops.login`).
Your purpose is to provide an immediate, high-density architectural snapshot of the project so any AI agent session starts with complete context without needing to scan the entire repository, and to update the **Iteration Log** after every major milestone.

---

# 1. Project Snapshot (`login-service`)
- **Stack:** Java 25, Spring Boot 3.4.5, Maven, Hexagonal Architecture (100% pure Java `domain`).
- **Database & Migrations:** Neon Serverless PostgreSQL (`users` table) + Spring Data JPA + Flyway (`V1__init_users_table.sql`).
- **Security:** Spring Security 6 (Stateless) + BCrypt (`BCryptPasswordEncoderAdapter`) + JJWT `0.12.6` HMAC-SHA256 (`JwtTokenAdapter`).
- **Messaging (Outbound):** Google Cloud Pub/Sub (`spring-cloud-gcp-starter-pubsub` `5.9.0`) publishing `UserCreatedEvent` JSON payloads with attributes `eventType=USER_CREATED` and `userId=<UUID>`.
- **Quality & CI/CD:** JaCoCo `0.8.13` (100% unit test coverage across 15 analyzed classes / 39 unit tests), Cucumber `7.15.0`, SonarCloud, Jib (`eclipse-temurin:25-jre-alpine` with `-XX:+UseCompactObjectHeaders` & `-XX:+UseSerialGC`), GitHub Actions (`.github/workflows/ci-cd.yml` with GCP Workload Identity Federation OIDC).

---

# 2. Hexagonal Component Map (`com.osvaldevops.login`)

## Domain Layer (`domain/` — Zero Spring Imports)
- **Models & Events:**
  - `domain/model/User.java`
  - `domain/event/UserCreatedEvent.java` (`userId`, `email`, `userName`, `role`, `createdAt`)
- **Exceptions:**
  - `InvalidCredentialsException` (`401`), `UserAccountDisabledException` (`403`), `UserAlreadyExistsException` (`409`), `EventPublicationException` (`503`).
- **Inbound Ports (`domain/port/in/`):**
  - `AuthenticateUserUseCase` (`LoginCommand` -> `AuthResult`)
  - `CreateUserUseCase` (`CreateUserCommand` -> `User`)
- **Outbound Ports (`domain/port/out/`):**
  - `UserRepositoryPort`, `PasswordEncoderPort`, `TokenProviderPort`, `UserEventPublisherPort`.
- **Domain Services (`domain/service/`):**
  - `AuthenticationDomainService`, `CreateUserDomainService`.

## Infrastructure Layer (`infrastructure/`)
- **Inbound Web Adapter (`adapter/in/web/`):**
  - `AuthController` (`POST /api/v1/auth/login`, `POST /api/v1/auth/register`)
  - `GlobalExceptionHandler` (`ApiErrorResponse`)
  - `AuthWebMapper` (MapStruct)
  - DTOs: `LoginRequest`, `AuthResponse`, `CreateUserRequest`, `UserResponse`, `ApiErrorResponse`
- **Outbound Adapters (`adapter/out/`):**
  - `messaging/PubSubUserEventPublisherAdapter` (`@Value("${app.pubsub.topics.user-created}")`)
  - `persistence/UserPersistenceAdapter`, `UserJpaEntity`, `UserPersistenceMapper`, `SpringDataUserRepository`
  - `security/JwtTokenAdapter`, `security/BCryptPasswordEncoderAdapter`
- **Configuration (`config/`):**
  - `BeanConfiguration` (wires `AuthenticationDomainService` and `CreateUserDomainService`)
  - `SecurityConfig` (stateless filter chain + `BCryptPasswordEncoder` bean)
  - `OpenApiConfig` (Swagger UI Bearer JWT scheme)

---

# 3. Environment Variables Contract (`application-dev.yml` & GitHub Secrets)
| Variable | Purpose |
| :--- | :--- |
| `DB_URL` | JDBC URL for Neon PostgreSQL |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `GCP_PROJECT_ID` | Google Cloud Project ID for Pub/Sub |
| `PUBSUB_USER_CREATED_TOPIC` | GCP Pub/Sub topic name for user creation events |
| `JWT_SECRET` | Base64-encoded 256/512-bit secret key for HMAC-SHA256 |
| `JWT_EXPIRATION_MS` | JWT token validity in milliseconds (default `3600000`) |
| `PORT` | HTTP server port injected by Cloud Run (default `8081`) |

---

# 4. Context Synthesis Protocol (Instructions for the Agent)
Whenever a major iteration is completed in `login-service`:
1. Read this file first before scanning the repository to save context tokens.
2. If new ports, adapters, endpoints, or environment variables were added, update Sections **2** and **3**.
3. Append a concise entry under **Section 5 (Iteration Log)** summarizing the date, objective, modified files, and test/coverage status.

---

# 5. Iteration Log
- **Iteration 1 (Unit Testing & Java 25 Compatibility):** Configured `maven-surefire-plugin` with ByteBuddy experimental flag for Java 25 and JaCoCo `prepare-agent`, reaching 100% unit test coverage.
- **Iteration 2 (Kafka to GCP Pub/Sub Migration):** Replaced Apache Kafka with `PubSubUserEventPublisherAdapter` (`spring-cloud-gcp-starter-pubsub`) publishing `UserCreatedEvent` to GCP Pub/Sub for downstream consumption by `notifications-service`.
- **Iteration 3 (Pure Hexagonal Domain & DevSecOps Hardening):** Extracted `PasswordEncoderPort` and `BCryptPasswordEncoderAdapter`, wired pure domain services via `BeanConfiguration`, removed hardcoded `@Value` fallbacks, masked all secrets in `application-dev.yml` for GitHub Actions OIDC/Secrets, and verified 39/39 unit tests with 100% JaCoCo coverage.
- **Iteration 4 (Cloud Run Memory Tuning & OpenAPI 3 Optimization):** Enabled Java 25 Virtual Threads, `lazy-initialization: true`, HikariCP `maximum-pool-size: 5`, Jib `eclipse-temurin:25-jre-alpine` with `-XX:+UseCompactObjectHeaders,-XX:+UseSerialGC`, enriched OpenAPI 3 `@Schema`/`@SecurityRequirements`/`@ApiResponses`, standardized all code/CI comments in English, and updated `README.md`.

