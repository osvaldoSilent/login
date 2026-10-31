# Login & User Service (`login-service`)

Servicio de **Autenticación y Registro de Usuarios** construido con **Java 25**, **Spring Boot 3.4.5**, **Arquitectura Hexagonal (Ports & Adapters)**, **Spring Security (Stateless JWT con JJWT)**, **Spring Data JPA**, **Flyway**, **PostgreSQL (Neon / Docker)** y documentado con **OpenAPI 3 / Swagger UI**.

---

## 1. Tecnologías Principales

| Tecnología | Versión / Detalle |
| :--- | :--- |
| **Java** | 25 |
| **Spring Boot** | 3.4.5 |
| **Spring Security** | 6.x (Stateless + `BCryptPasswordEncoder`) |
| **JWT** | `io.jsonwebtoken:jjwt` `0.12.6` (HMAC-SHA256) |
| **Persistencia** | Spring Data JPA (Hibernate) + PostgreSQL + Flyway |
| **Mapeo & Boilerplate** | MapStruct `1.6.3` + Lombok |
| **Documentación API** | SpringDoc OpenAPI `2.8.6` (Swagger UI) |
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
    │   │   │   ├── exception/
    │   │   │   │   ├── InvalidCredentialsException.java
    │   │   │   │   └── UserAlreadyExistsException.java
    │   │   │   ├── model/
    │   │   │   │   └── User.java
    │   │   │   ├── port/
    │   │   │   │   ├── in/
    │   │   │   │   │   ├── AuthenticateUserUseCase.java
    │   │   │   │   │   └── CreateUserUseCase.java
    │   │   │   │   └── out/
    │   │   │   │       ├── TokenProviderPort.java
    │   │   │   │       └── UserRepositoryPort.java
    │   │   │   └── service/
    │   │   │       ├── AuthenticationDomainService.java
    │   │   │       └── CreateUserDomainService.java
    │   │   └── infrastructure/
    │   │       ├── adapter/
    │   │       │   ├── in/web/
    │   │       │   │   ├── controller/AuthController.java
    │   │       │   │   ├── dto/ (LoginRequest, AuthResponse, CreateUserRequest, UserResponse)
    │   │       │   │   └── mapper/AuthWebMapper.java
    │   │       │   └── out/
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
                ├── in/web/controller/AuthControllerTest.java
                └── out/
                    ├── persistence/UserPersistenceAdapterTest.java
                    └── security/JwtTokenAdapterTest.java
```

---

## 3. Documentación Interactiva con Swagger UI / OpenAPI

Con la aplicación en ejecución (puerto `8081`), puedes explorar y probar todos los endpoints desde tu navegador:

- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **OpenAPI JSON Spec:** http://localhost:8081/v3/api-docs

---

## 4. Endpoints Principales

### 4.1 Registrar un nuevo usuario (`POST /api/v1/auth/register`)

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

**Response (`201 Created`):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "osval@osvaldevops.com",
  "userName": "osval",
  "role": "ROLE_USER",
  "enabled": true,
  "createdAt": "2026-10-30T18:30:00Z"
}
```

### 4.2 Iniciar sesión (`POST /api/v1/auth/login`)

**Request:**
```powershell
curl -X POST http://localhost:8081/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "osval@osvaldevops.com",
    "password": "secretPassword123"
  }'
```

**Response (`200 OK`):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresInMs": 3600000
}
```

---

## 5. Esquema de Base de Datos (`users`)

| Columna | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | `UUID` (PK) | Identificador único del usuario |
| `email` | `VARCHAR(255)` (UNIQUE) | Correo electrónico |
| `password` | `VARCHAR(255)` | Hash BCrypt de la contraseña |
| `user_name` | `VARCHAR(255)` | Nombre de usuario |
| `role` | `VARCHAR(50)` | Rol asignado (por defecto `ROLE_USER`) |
| `enabled` | `BOOLEAN` | Estado de la cuenta (`true` / `false`) |
| `created_at` | `TIMESTAMPTZ` | Fecha de creación |
| `updated_at` | `TIMESTAMPTZ` | Fecha de última actualización |

---

## 6. Ejecución y Pruebas Unitarias

### Ejecutar la aplicación
Desde VS Code (**Run/Debug** sobre `LoginServiceApplication`) o con Maven:
```powershell
cd login-service
./mvnw spring-boot:run
```

### Ejecutar las pruebas unitarias
Puedes ejecutar todas las suites de pruebas desde el panel **Testing** de VS Code o por línea de comandos:
```powershell
cd login-service
./mvnw test
```
