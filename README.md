# Login & User Service (`login-service`)

Servicio de **Autenticación y Registro de Usuarios** construido con **Java 25**, **Spring Boot 3.4.5**, **Arquitectura Hexagonal (Ports & Adapters)**, **Spring Security (Stateless JWT con JJWT)**, **Spring Data JPA**, **Flyway**, **PostgreSQL (Neon / Docker)**, **Google Cloud Pub/Sub**, documentado con **OpenAPI 3 / Swagger UI**, y asegurado en calidad con **JaCoCo** y **SonarQube**.

---

## 1. Tecnologías Principales

| Tecnología | Versión / Detalle |
| :--- | :--- |
| **Java** | 25 |
| **Spring Boot** | 3.4.5 |
| **Spring Security** | 6.x (Stateless + `BCryptPasswordEncoder`) |
| **JWT** | `io.jsonwebtoken:jjwt` `0.12.6` (HMAC-SHA256) |
| **Persistencia** | Spring Data JPA (Hibernate) + PostgreSQL + Flyway |
| **Mensajería Cloud-Native** | Spring Cloud GCP Pub/Sub (`spring-cloud-gcp-starter-pubsub`) |
| **Mapeo & Boilerplate** | MapStruct `1.6.3` + Lombok |
| **Documentación API** | SpringDoc OpenAPI `2.8.6` (Swagger UI) |
| **Calidad & Cobertura** | JaCoCo `0.8.13` (mínimo 80%) + SonarQube Scanner `5.0.0.4389` |
| **Testing** | JUnit 5, Mockito, AssertJ |

---

## 2. Arquitectura Hexagonal (`com.osvaldevops.login`)

```text
login-service/
├── compose.yaml
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/osvaldevops/login/
    │   │   ├── LoginServiceApplication.java
    │   │   ├── domain/
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
    │   │   │   │       ├── TokenProviderPort.java
    │   │   │   │       ├── UserEventPublisherPort.java
    │   │   │   │       └── UserRepositoryPort.java
    │   │   │   └── service/
    │   │   │       ├── AuthenticationDomainService.java
    │   │   │       └── CreateUserDomainService.java
    │   │   └── infrastructure/
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
    │   │       │       └── security/JwtTokenAdapter.java
    │   │       └── config/
    │   │           ├── OpenApiConfig.java
    │   │           └── SecurityConfig.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       └── db/migration/V1__init_users_table.sql
    └── test/
        └── java/com/osvaldevops/login/
            ├── LoginServiceApplicationTests.java
            ├── domain/service/
            │   ├── AuthenticationDomainServiceTest.java
            │   └── CreateUserDomainServiceTest.java
            └── infrastructure/adapter/
                ├── in/web/
                │   ├── controller/AuthControllerTest.java
                │   ├── exception/GlobalExceptionHandlerTest.java
                │   └── mapper/AuthWebMapperTest.java
                └── out/
                    ├── messaging/PubSubUserEventPublisherAdapterTest.java
                    ├── persistence/
                    │   ├── UserPersistenceAdapterTest.java
                    │   └── mapper/UserPersistenceMapperTest.java
                    └── security/JwtTokenAdapterTest.java
```

---

## 3. Integración con Google Cloud Pub/Sub (Eventos para Notificaciones)

Cuando se crea un usuario mediante `POST /api/v1/auth/register`:
1. [`CreateUserDomainService`](src/main/java/com/osvaldevops/login/domain/service/CreateUserDomainService.java) guarda el usuario en PostgreSQL.
2. Publica un evento [`UserCreatedEvent`](src/main/java/com/osvaldevops/login/domain/event/UserCreatedEvent.java) a través del puerto de salida [`UserEventPublisherPort`](src/main/java/com/osvaldevops/login/domain/port/out/UserEventPublisherPort.java).
3. El adaptador [`PubSubUserEventPublisherAdapter`](src/main/java/com/osvaldevops/login/infrastructure/adapter/out/messaging/PubSubUserEventPublisherAdapter.java) publica el mensaje JSON en el topic de **Google Cloud Pub/Sub** con los atributos `eventType=USER_CREATED` y `userId=<UUID>`, para que [`notifications-service`](../notifications-service) lo reciba vía Push en Cloud Run.

### Configuración del Topic y Contrato del Mensaje
- **Proyecto GCP:** configurable vía `GCP_PROJECT_ID`
- **Topic:** `user-created-topic` (configurable vía `PUBSUB_USER_CREATED_TOPIC`)
- **Atributos del mensaje:** `eventType: USER_CREATED`, `userId: <UUID>`
- **Payload JSON publicado al topic:**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "osval@osvaldevops.com",
  "userName": "osval",
  "role": "ROLE_USER",
  "createdAt": "2026-10-30T19:00:00Z"
}
```

### Levantar el Emulador de GCP Pub/Sub localmente con Docker Compose
```powershell
cd login-service
docker compose up -d pubsub-emulator
```
