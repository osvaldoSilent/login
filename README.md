# Login & User Service (`login-service`)

Servicio de **Autenticación y Registro de Usuarios** construido con **Java 25**, **Spring Boot 3.4.5**, **Arquitectura Hexagonal (Ports & Adapters)**, **Spring Security (Stateless JWT con JJWT)**, **Spring Data JPA**, **Flyway**, **PostgreSQL (Neon / Docker)**, **Apache Kafka**, documentado con **OpenAPI 3 / Swagger UI**, y asegurado en calidad con **JaCoCo** y **SonarQube**.

---

## 1. Tecnologías Principales

| Tecnología | Versión / Detalle |
| :--- | :--- |
| **Java** | 25 |
| **Spring Boot** | 3.4.5 |
| **Spring Security** | 6.x (Stateless + `BCryptPasswordEncoder`) |
| **JWT** | `io.jsonwebtoken:jjwt` `0.12.6` (HMAC-SHA256) |
| **Persistencia** | Spring Data JPA (Hibernate) + PostgreSQL + Flyway |
| **Mensajería Asíncrona** | Spring for Apache Kafka (`spring-kafka`) |
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
    │   │       │       │   └── KafkaUserEventPublisherAdapter.java
    │   │       │       ├── persistence/
    │   │       │       │   ├── UserPersistenceAdapter.java
    │   │       │       │   ├── entity/UserJpaEntity.java
    │   │       │       │   ├── mapper/UserPersistenceMapper.java
    │   │       │       │   └── repository/SpringDataUserRepository.java
    │   │       │       └── security/JwtTokenAdapter.java
    │   │       └── config/
    │   │           ├── KafkaTopicConfig.java
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
                │   └── exception/GlobalExceptionHandlerTest.java
                └── out/
                    ├── messaging/KafkaUserEventPublisherAdapterTest.java
                    ├── persistence/UserPersistenceAdapterTest.java
                    └── security/JwtTokenAdapterTest.java
```

---

## 3. Integración con Apache Kafka (Eventos para Notificaciones)

Cuando se crea un usuario mediante `POST /api/v1/auth/register`:
1. [`CreateUserDomainService`](src/main/java/com/osvaldevops/login/domain/service/CreateUserDomainService.java) guarda el usuario en PostgreSQL.
2. Publica un evento [`UserCreatedEvent`](src/main/java/com/osvaldevops/login/domain/event/UserCreatedEvent.java) a través del puerto de salida [`UserEventPublisherPort`](src/main/java/com/osvaldevops/login/domain/port/out/UserEventPublisherPort.java).
3. El adaptador [`KafkaUserEventPublisherAdapter`](src/main/java/com/osvaldevops/login/infrastructure/adapter/out/messaging/KafkaUserEventPublisherAdapter.java) envía el mensaje en formato JSON al topic de Kafka para que la **API de Notificaciones** lo consuma.

### Configuración del Topic y Contrato del Mensaje
- **Broker por defecto:** `localhost:9092` (configurable vía `KAFKA_BOOTSTRAP_SERVERS`)
- **Topic:** `user-created-events` (configurable vía `KAFKA_USER_CREATED_TOPIC`)
- **Key del mensaje:** `userId` (`String`)
- **Payload JSON enviado al topic:**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "osval@osvaldevops.com",
  "userName": "osval",
  "role": "ROLE_USER",
  "createdAt": "2026-10-30T19:00:00Z"
}
```

### Levantar Kafka localmente con Docker Compose
```powershell
cd login-service
docker compose up -d kafka
```

---

## 4. Documentación Interactiva con Swagger UI / OpenAPI

Con la aplicación en ejecución (puerto `8081`), puedes explorar y probar todos los endpoints desde tu navegador:

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **OpenAPI JSON Spec:** http://localhost:8081/v3/api-docs

---

## 5. Endpoints Principales

### 5.1 Registrar un nuevo usuario (`POST /api/v1/auth/register`)

**Request:**
```powershell
curl -X POST http://localhost:8081/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "osval@osvaldevops.com",
    "password": "secretPassword123",
    "userName": "osval",
    "role": "ROLE_USER"
  }'
```

### 5.2 Iniciar sesión (`POST /api/v1/auth/login`)

**Request:**
```powershell
curl -X POST http://localhost:8081/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "osval@osvaldevops.com",
    "password": "secretPassword123"
  }'
```

---

## 6. Cobertura de Código (JaCoCo) y Análisis Estático (SonarQube)

### 6.1 Generar el reporte de cobertura con JaCoCo
Al ejecutar las pruebas unitarias, **JaCoCo** genera automáticamente los reportes HTML y XML:

```powershell
cd login-service
./mvnw clean test
```
- **Reporte visual HTML:** `login-service/target/site/jacoco/index.html`
- **Reporte XML (para SonarQube):** `login-service/target/site/jacoco/jacoco.xml`
- **Verificar umbral mínimo de cobertura (80%):**
  ```powershell
  ./mvnw verify
  ```

### 6.2 Ejecutar SonarQube localmente y enviar el análisis
1. Levanta el contenedor de **SonarQube Community** incluido en `compose.yaml`:
   ```powershell
   cd login-service
   docker compose up -d sonarqube
   ```
2. Abre http://localhost:9000 en tu navegador (credenciales iniciales: `admin` / `admin`), genera un token de proyecto y ejecuta:
   ```powershell
   ./mvnw clean verify sonar:sonar "-Dsonar.token=TU_TOKEN_DE_SONARQUBE"
   ```
