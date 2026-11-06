# 🔐 Login & User Registration Service (`login-service`)

Cloud-Native **Authentication (JWT) & User Registration** microservice built with **Java 25**, **Spring Boot 3.4.5**, **Pure Hexagonal Architecture (Ports & Adapters)**, **Spring Security 6 (Stateless + BCrypt)**, **Spring Data JPA + Flyway**, **PostgreSQL (Neon Serverless / Docker)**, and **Google Cloud Pub/Sub**, optimized for low memory footprint on **Google Cloud Run**, documented with **OpenAPI 3 / Swagger UI**, and quality-gated with **JaCoCo (100% coverage)** and **SonarCloud**.

---

## 📋 Table of Contents
1. [Technology Stack](#-1-technology-stack)
2. [Pure Hexagonal Architecture](#-2-pure-hexagonal-architecture-comosvaldevopslogin)
3. [Prerequisites](#-3-prerequisites)
4. [Required Secrets & Environment Variables](#-4-required-secrets--environment-variables)
5. [Usage Guide: Local Execution & Cloud Run Deployment](#-5-usage-guide-local-execution--cloud-run-deployment)
6. [API Consumption Guide (Swagger UI, Postman & cURL)](#-6-api-consumption-guide-swagger-ui-postman--curl)
7. [Event-Driven Integration with Google Cloud Pub/Sub](#-7-event-driven-integration-with-google-cloud-pubsub)
8. [Security & Memory Optimization (Cloud Run)](#-8-security--memory-optimization-cloud-run)
9. [AI Agent Documentation & Engineering Rules (`.antigravity/*.md`)](#-9-ai-agent-documentation--engineering-rules-antigravitymd)

---

## 🛠️ 1. Technology Stack

| Category | Technology | Version / Details |
| :--- | :--- | :--- |
| **Language & Runtime** | Java (Eclipse Temurin) | **25** (Virtual Threads + Compact Object Headers `-XX:+UseCompactObjectHeaders`) |
| **Core Framework** | Spring Boot | **3.4.5** (`lazy-initialization: true`, `open-in-view: false`) |
| **Security & Auth** | Spring Security 6 + JJWT | Stateless Filter Chain + `BCryptPasswordEncoder` + `io.jsonwebtoken:jjwt` `0.12.6` (`HMAC-SHA256`) |
| **Persistence** | Spring Data JPA + Flyway + PostgreSQL | Neon Serverless PostgreSQL + HikariCP (`minimum-idle: 0`, `maximum-pool-size: 5`) |
| **Async Messaging** | Spring Cloud GCP Pub/Sub | `spring-cloud-gcp-starter-pubsub` (`5.9.0`) |
| **Mapping & Boilerplate** | MapStruct + Lombok | MapStruct `1.6.3` (`componentModel = "spring"`) |
| **API Documentation** | SpringDoc OpenAPI 3 | `springdoc-openapi-starter-webmvc-ui` `2.8.6` (`/swagger-ui.html`, `/v3/api-docs`) |
| **Quality & Testing** | JUnit 5, Mockito, AssertJ, Cucumber, JaCoCo, SonarQube | **39 unit tests** (100% JaCoCo coverage across all 15 analyzed classes) + SonarCloud Quality Gate |
| **Container & CI/CD** | Jib + GitHub Actions + Cloud Run | `eclipse-temurin:25-jre-alpine` + Workload Identity Federation (keyless OIDC) |

---

## 🏛️ 2. Pure Hexagonal Architecture (`com.osvaldevops.login`)

The `domain` package is **100% pure Java** (zero imports from `org.springframework.*`, Jakarta JPA, or infrastructure libraries). Wiring between domain services and outbound ports is handled exclusively in [`BeanConfiguration`](src/main/java/com/osvaldevops/login/infrastructure/config/BeanConfiguration.java).

```text
login-service/
├── .antigravity/
│   ├── context-synthesizer.md
│   └── security-and-memory-checklist.md
├── .github/workflows/
│   └── ci-cd.yml
├── docs/
│   └── postman_collection.json                      # Ready-to-import Postman v2.1 Collection
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
    │       ├── application.yml                          # Ignored in Git (local development)
    │       ├── application-dev.yml                      # Version-controlled with ${ENV_VAR} placeholders
    │       └── db/migration/V1__init_users_table.sql    # Auto-executed by Flyway on startup
    └── test/
        └── java/com/osvaldevops/login/                  # 39 Unit Tests (100% JaCoCo Coverage)
```

---

## ⚙️ 3. Prerequisites

Before cloning and running this service locally, make sure you have the following installed:

- **Java 25** (Eclipse Temurin JDK 25 recommended — verify with `java -version`)
- **Apache Maven 3.9+** (verify with `mvn -version`)
- **Docker & Docker Compose** (required to spin up the local PostgreSQL database, GCP Pub/Sub emulator, and SonarQube)
- **Git**

---

## 🔑 4. Required Secrets & Environment Variables

[`src/main/resources/application-dev.yml`](src/main/resources/application-dev.yml) is tracked in Git with **zero plain-text credentials**; all sensitive settings are resolved from environment variables injected via **GitHub Secrets** and **Google Cloud Secret Manager**.

### A. Application Environment Variables (`application-dev.yml`)
| Variable | Description | Example |
| :--- | :--- | :--- |
| **`DB_URL`** | PostgreSQL JDBC URL (Neon Serverless or local instance). | `jdbc:postgresql://ep-xyz.us-east-2.aws.neon.tech/neondb?sslmode=require` |
| **`DB_USERNAME`** | PostgreSQL database username. | `neondb_owner` |
| **`DB_PASSWORD`** | PostgreSQL database password (injected in Cloud Run from GCP Secret Manager `db-password:latest`). | `********` |
| **`GCP_PROJECT_ID`** | Google Cloud Project ID hosting the Pub/Sub topic. | `project-d168a88d-1130-49df-8d8` |
| **`PUBSUB_USER_CREATED_TOPIC`** | Google Cloud Pub/Sub topic name for user registration events. | `user-created-topic` |
| **`JWT_SECRET`** | Base64-encoded secret key of at least 256 bits (injected in Cloud Run from GCP Secret Manager `jwt-secret:latest`). | `bXlTdXBlclNlY3JldEtleUZvckpXVDI1NkJpdHNPbmx5...` |
| **`JWT_EXPIRATION_MS`** | JWT token validity duration in milliseconds (defaults to `3600000` = 1 hour). | `3600000` |
| **`PORT`** | HTTP server port (automatically injected by Cloud Run; defaults to `8081`). | `8081` |

### B. CI/CD Secrets in GitHub Actions (`Settings -> Secrets and variables -> Actions`)
To enable SonarCloud analysis and keyless GCP deployment in [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml):
- **SonarCloud**: `SONAR_TOKEN`, `SONAR_PROJECT_KEY`, `SONAR_ORGANIZATION_KEY`.
- **Google Cloud OIDC (Workload Identity Federation)**:
  - `GCP_WIF_PROVIDER`: Full WIF provider resource name (`projects/<NUM>/locations/global/workloadIdentityPools/<POOL>/providers/<PROVIDER>`).
  - `GCP_SERVICE_ACCOUNT`: Deployment service account email (`github-actions-sa@<PROJECT_ID>.iam.gserviceaccount.com`).

### C. How to Generate a Secure Base64 `JWT_SECRET`
Run either command below to generate a cryptographically secure 64-byte (512-bit) Base64-encoded key:

```powershell
# Windows PowerShell:
$bytes = New-Object Byte[] 64; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes); [Convert]::ToBase64String($bytes)
```
```bash
# Linux / macOS / Git Bash:
openssl rand -base64 64 | tr -d '\n'
```

---

## 🚀 5. Usage Guide: Local Execution & Cloud Run Deployment

### Option 1: Local Development Execution (`application.yml`)
`src/main/resources/application.yml` is listed in [`.gitignore`](.gitignore) so you can safely store local development credentials on your machine without committing them.

1. **Start Local PostgreSQL & Pub/Sub Emulator with Docker Compose (Optional if connecting directly to Neon/GCP)**:
   ```powershell
   cd c:\dev\practice\login-service
   docker compose up -d postgres pubsub-emulator
   ```

2. **Database Migrations (Automatic via Flyway)**:
   > **Note:** You do **not** need to create any tables or indexes manually. **Flyway** (`spring.flyway.enabled: true`) automatically executes [`src/main/resources/db/migration/V1__init_users_table.sql`](src/main/resources/db/migration/V1__init_users_table.sql) when Spring Boot starts up, creating the `users` table and `idx_users_email` index if they do not exist.

3. **Run the Microservice with the `dev` Profile and Environment Variables in PowerShell**:
   ```powershell
   $env:SPRING_PROFILES_ACTIVE="dev"
   $env:DB_URL="jdbc:postgresql://localhost:5432/login_db"
   $env:DB_USERNAME="login_user"
   $env:DB_PASSWORD="login_secret_password"
   $env:GCP_PROJECT_ID="osvaldevops-local"
   $env:PUBSUB_USER_CREATED_TOPIC="user-created-topic"
   $env:JWT_SECRET="dW5pdC10ZXN0LWR1bW15LWp3dC1zZWNyZXQta2V5LWZvci1obWFjLXNoYTI1Ni12ZXJpZmljYXRpb24tb25seQ=="

   mvn spring-boot:run
   ```
   *(Or if your local `src/main/resources/application.yml` is already configured, simply run `mvn spring-boot:run`).*

4. **Run Unit Tests & JaCoCo Coverage Verification (100%)**:
   ```powershell
   mvn verify
   ```
   The HTML coverage report is generated at `target/site/jacoco/index.html`.

---

### Option 2: Controlled Deployment to Google Cloud Run via GitHub Actions (`[deploy]`)
The [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml) pipeline uses a **commit-message deployment gate** to prevent unintended Cloud Run deployments:

1. **Verification Commit (Runs Unit Tests + JaCoCo + SonarCloud Quality Gate only)**:
   ```bash
   git add .
   git commit -m "refactor: update authentication validation"
   git push origin main
   ```
2. **Production Deployment Commit (Runs Tests + SonarCloud + Jib Alpine Build + Cloud Run Deploy)**:
   Include the **`[deploy]`** tag anywhere in your commit message when pushing to `main`:
   ```bash
   git add .
   git commit -m "feat: release login-service v1.0 [deploy]"
   git push origin main
   ```

---

## 🌐 6. API Consumption Guide (Swagger UI, Postman & cURL)

Once the service is running (default port `8081`):
- **Interactive Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON Spec:** `http://localhost:8081/v3/api-docs`
- **Actuator Health Check:** `http://localhost:8081/actuator/health`
- **Postman / Insomnia Collection:** Import [`docs/postman_collection.json`](docs/postman_collection.json) directly into Postman or Insomnia. It comes pre-configured with the `{{baseUrl}}` variable (`http://localhost:8081`) and a post-response script on the Login request that automatically stores the issued JWT in `{{accessToken}}`.

### 1. Register a New User (`POST /api/v1/auth/register`)
Persists the new user in PostgreSQL (with a BCrypt-hashed password) and publishes a `UserCreatedEvent` to Google Cloud Pub/Sub:

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "osval@osvaldevops.com",
    "password": "secretPassword123",
    "userName": "osval",
    "role": "ROLE_USER"
  }'
```

**Success Response (`201 Created`):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "osval@osvaldevops.com",
  "userName": "osval",
  "role": "ROLE_USER",
  "enabled": true,
  "createdAt": "2026-11-06T16:45:00Z"
}
```

### 2. Authenticate User & Obtain JWT (`POST /api/v1/auth/login`)
Validates user credentials against PostgreSQL and issues an `HMAC-SHA256` signed Bearer JWT token:

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "osval@osvaldevops.com",
    "password": "secretPassword123"
  }'
```

**Success Response (`200 OK`):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvc3ZhbEBvc3ZhbGRldm9wcy5jb20iLCJ1c2VySWQiOiI1NTBlODQwMC...",
  "tokenType": "Bearer",
  "expiresInMs": 3600000
}
```

> **Note on Token Lifecycle & Refresh Tokens:**  
> By design, `login-service` implements a **100% stateless JWT architecture** without server-side session storage or refresh tokens. The `accessToken` is valid for `expiresInMs` (`3600000 ms` = **1 hour** by default, configurable via `JWT_EXPIRATION_MS`). Once the token expires, the client application must prompt the user to re-authenticate via `POST /api/v1/auth/login` to obtain a new token.

### 3. HTTP Status Code Summary
| Endpoint | Success Code | Documented Error Codes (`ApiErrorResponse`) |
| :--- | :--- | :--- |
| `POST /api/v1/auth/register` | `201 Created` | `400` (Validation / Malformed JSON), `409` (Duplicate email / Integrity constraint), `500` (Internal error), `503` (DB or Pub/Sub unavailable) |
| `POST /api/v1/auth/login` | `200 OK` | `400` (Validation / Malformed JSON), `401` (Invalid email or password), `403` (Account disabled), `500` (Internal error), `503` (DB unavailable) |

---

## 📨 7. Event-Driven Integration with Google Cloud Pub/Sub

When a new user registers via `POST /api/v1/auth/register`:
1. [`CreateUserDomainService`](src/main/java/com/osvaldevops/login/domain/service/CreateUserDomainService.java) verifies uniqueness via `existsByEmail`, hashes the password through [`PasswordEncoderPort`](src/main/java/com/osvaldevops/login/domain/port/out/PasswordEncoderPort.java), and saves the user in PostgreSQL.
2. Emits a [`UserCreatedEvent`](src/main/java/com/osvaldevops/login/domain/event/UserCreatedEvent.java) to [`UserEventPublisherPort`](src/main/java/com/osvaldevops/login/domain/port/out/UserEventPublisherPort.java).
3. [`PubSubUserEventPublisherAdapter`](src/main/java/com/osvaldevops/login/infrastructure/adapter/out/messaging/PubSubUserEventPublisherAdapter.java) publishes the JSON payload to the configured topic (`${PUBSUB_USER_CREATED_TOPIC}`) with attributes `eventType=USER_CREATED` and `userId=<UUID>`, enabling [`notifications-service`](../notifications-service) to dispatch the welcome email.

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "osval@osvaldevops.com",
  "userName": "osval",
  "role": "ROLE_USER",
  "createdAt": "2026-11-06T16:45:00Z"
}
```

---

## 🛡️ 8. Security & Memory Optimization (Cloud Run)

### Security & Secrets (Zero-Trust)
- **Configuration Isolation**: `src/main/resources/application.yml` is ignored in [`.gitignore`](.gitignore) for local testing; [`src/main/resources/application-dev.yml`](src/main/resources/application-dev.yml) is tracked in Git with all credentials masked via environment variables.
- **No Hardcoded `@Value` Fallbacks**: [`JwtTokenAdapter`](src/main/java/com/osvaldevops/login/infrastructure/adapter/out/security/JwtTokenAdapter.java) and [`PubSubUserEventPublisherAdapter`](src/main/java/com/osvaldevops/login/infrastructure/adapter/out/messaging/PubSubUserEventPublisherAdapter.java) strictly require properties from the environment without hardcoded defaults in Java.
- **Log Protection & Anti-Enumeration**: `spring.jpa.show-sql: false`, `org.springframework.security: INFO`, generic error messages for invalid credentials, and account status check (`!user.isEnabled()`) prior to BCrypt evaluation.

### Memory Footprint & Cold-Start Tuning (Java 25 + Cloud Run `512Mi`)
- **Alpine JRE 25 Base Image**: Daemonless container build via Jib using `eclipse-temurin:25-jre-alpine` (~95 MB).
- **Container JVM Flags**: `-XX:+UseCompactObjectHeaders` (shrinks object headers to 64 bits in Java 25), `-XX:+UseSerialGC`, `-XX:MaxRAMPercentage=75.0`, and `-XX:TieredStopAtLevel=1`.
- **Spring Boot & Serverless HikariCP Diet**: `spring.main.lazy-initialization: true`, `spring.threads.virtual.enabled: true`, `spring.jpa.open-in-view: false`, `minimum-idle: 0`, and `maximum-pool-size: 5` (enabling Neon PostgreSQL *scale-to-zero*).

---

## 🤖 9. AI Agent Documentation & Engineering Rules (`.antigravity/*.md`)

### A. Project-Specific Rules ([`login-service/.antigravity/`](.antigravity))
| `.md` File | Description |
| :--- | :--- |
| [`.antigravity/context-synthesizer.md`](.antigravity/context-synthesizer.md) | **Iteration Context Synthesizer:** High-density architectural snapshot, hexagonal component map, environment variable contract, and iteration log for `login-service`. |
| [`.antigravity/security-and-memory-checklist.md`](.antigravity/security-and-memory-checklist.md) | **Security & Memory Checklist:** Pre-deployment verification checklist covering secret masking, JWT/BCrypt hardening, Neon HikariCP tuning, and Cloud Run JVM flags. |

### B. Workspace-Wide Rules ([`../.antigravity/`](../.antigravity))
| `.md` File | Description |
| :--- | :--- |
| [`../.antigravity/spring_boot_init.md`](../.antigravity/spring_boot_init.md) | **Cloud-Native Initialization Blueprint:** Core standards for microservices built with Java 25, Spring Boot 3.4+, Neon Postgres, GCP Pub/Sub, and serverless deployment. |
| [`../.antigravity/hexagonal-validator.md`](../.antigravity/hexagonal-validator.md) | **Hexagonal Architecture Validator:** Strict purity rules for the `domain` package (zero Spring imports), inbound/outbound ports, and `BeanConfiguration` wiring. |
| [`../.antigravity/unit-test-agent.md`](../.antigravity/unit-test-agent.md) | **Unit Testing Agent:** Guidelines for JUnit 5, Mockito, AssertJ, and Java 25 Surefire/ByteBuddy configuration to achieve $\ge 80\%$ (100% actual) JaCoCo coverage. |
| [`../.antigravity/security-and-secrets-auditor.md`](../.antigravity/security-and-secrets-auditor.md) | **DevSecOps & Secrets Auditor:** Credential leak prevention, `${ENV_VAR}` placeholders, GitHub Secrets, and Workload Identity Federation (OIDC). |
| [`../.antigravity/gcp-cloudrun-pubsub-architect.md`](../.antigravity/gcp-cloudrun-pubsub-architect.md) | **GCP Cloud Run & Pub/Sub Architect:** Best practices for idempotent Pub/Sub publishing/consuming, Jib builds, and Cloud Run scaling configuration. |
| [`../.antigravity/bdd-cucumber-agent.md`](../.antigravity/bdd-cucumber-agent.md) | **Cucumber BDD Agent:** Guidelines for Gherkin (`.feature`) scenarios and acceptance tests decoupled from external infrastructure. |
| [`../.antigravity/sonarqube-quality-gate.md`](../.antigravity/sonarqube-quality-gate.md) | **SonarQube/SonarCloud Quality Gate:** Rules to maintain `0` Bugs, `0` Vulnerabilities, `0` Security Hotspots, and clean code in CI/CD. |
| [`../.antigravity/api-contract-and-docs.md`](../.antigravity/api-contract-and-docs.md) | **REST Contract & OpenAPI 3 Standard:** Rules for `record` DTO design, centralized `ApiErrorResponse` handling, and Swagger UI documentation. |
| [`../.antigravity/code-generation-behavior.md`](../.antigravity/code-generation-behavior.md) | **Code Generation Discipline (Minimal Diff):** Behavioral rule enforcing surgical code changes strictly scoped to the requested business logic. |
