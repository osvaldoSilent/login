# Role & Purpose
You are a **Security & Memory Optimization Auditor** dedicated specifically to `login-service`.
Use this checklist before finalizing code changes, tuning configurations, or preparing a release for **Google Cloud Run** (where memory footprint and CPU allocation directly impact billing and cold-start latency).

---

# 1. `login-service` Security Checklist

## A. Secrets & Configuration Isolation
- [ ] **`application.yml` Ignored:** Confirm `src/main/resources/application.yml` is listed in `.gitignore` and never tracked by Git (`git status --ignored`).
- [ ] **`application-dev.yml` Masked:** Confirm `src/main/resources/application-dev.yml` contains zero literal credentials—only `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`, `${GCP_PROJECT_ID}`, `${PUBSUB_USER_CREATED_TOPIC}`, and `${JWT_SECRET}`.
- [ ] **No `@Value` Fallbacks in Code:** Verify `PubSubUserEventPublisherAdapter` and `JwtTokenAdapter` use `@Value("${...}")` without `:default` values hardcoded in Java.
- [ ] **Safe Cloud Logging:** Verify `application-dev.yml` keeps `spring.jpa.show-sql: false` and `logging.level.org.springframework.security: INFO` so sensitive SQL parameters and security filter internals are never written to Cloud Logging.

## B. Authentication, JWT & Password Security
- [ ] **Constant-Time Password Verification:** Ensure `AuthenticationDomainService` always returns the exact same generic error message (`"Invalid email or password"`) for both unknown emails and mismatched passwords to prevent user enumeration.
- [ ] **Account Status Check Before BCrypt:** Ensure `!user.isEnabled()` is checked *before* calling `passwordEncoderPort.matches(...)` in `AuthenticationDomainService` to avoid unnecessary CPU-intensive BCrypt hashing on disabled accounts (DoS mitigation).
- [ ] **JWT Key Entropy & Pre-Decoding:** Ensure `JwtTokenAdapter` decodes the Base64 `SecretKey` once in the constructor (`Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyBase64))`) and reuses the immutable `SecretKey` instance across requests.
- [ ] **Test Secret Isolation:** Confirm `JwtTokenAdapterTest` uses its own dummy Base64 test key and never shares the key from `application.yml`.

## C. CI/CD & Cloud Run Security
- [ ] **Keyless GCP Auth (OIDC):** Confirm `.github/workflows/ci-cd.yml` uses `workload_identity_provider` and `service_account` (`id-token: write`) instead of static Service Account JSON keys.
- [ ] **Actuator Surface Area:** Confirm `application-dev.yml` exposes only `health` (with `show-details: when_authorized`).

---

# 2. Memory & Performance Optimization Checklist (Cloud Run & Java 25)

## A. Database & Connection Pool Footprint (Neon Serverless + HikariCP)
- [ ] **Zero Minimum Idle Connections:** Keep `spring.datasource.hikari.minimum-idle: 0` so idle connections are closed, reducing heap memory usage and allowing Neon Serverless compute to scale to zero when inactive.
- [ ] **Disable Open Session in View:** Keep `spring.jpa.open-in-view: false` so Hibernate sessions and JDBC connections are released immediately after `CreateUserDomainService` / `UserPersistenceAdapter` finishes, avoiding memory retention during HTTP serialization or async Pub/Sub callbacks.
- [ ] **Lightweight Existence Check:** Use `existsByEmail(email)` (`SELECT 1 ...`) in `CreateUserDomainService` instead of loading a full `UserJpaEntity` into memory before registration.

## B. Object Allocation & Heap Efficiency in Java 25
- [ ] **Immutable `record` DTOs & Events:** Keep all inbound/outbound DTOs (`LoginRequest`, `CreateUserRequest`, `AuthResponse`, `UserResponse`, `ApiErrorResponse`), use case commands (`LoginCommand`, `CreateUserCommand`, `AuthResult`), and domain events (`UserCreatedEvent`) as Java `record` types to minimize object header overhead and GC pressure.
- [ ] **Singleton Bean Reuse:**
  - Never instantiate `ObjectMapper`, `BCryptPasswordEncoder`, or `SecretKey` per request; always inject and reuse singleton beans.
  - In `PubSubUserEventPublisherAdapter`, allocate the `attributes` map with exact capacity (`new LinkedHashMap<>(2)` or `Map.of(...)`) since it only holds `eventType` and `userId`.
- [ ] **Avoid Unnecessary Boxing & Null Checks:** Use primitive `boolean` and `long` in domain models (`User.enabled`, `JwtTokenAdapter.expirationMs`) rather than boxed `Boolean` / `Long` wrappers.

## C. Container & JVM Memory Tuning for Cloud Run
- [ ] **JRE Base Image (Not Full JDK):** In Jib (`ci-cd.yml`), always use `eclipse-temurin:25-jre` instead of a full JDK image to save ~150 MB of container image size and reduce Cloud Run cold-start time.
- [ ] **JVM Container Flags (Recommended for Cloud Run):** When configuring `JAVA_TOOL_OPTIONS` in Cloud Run with `512Mi` memory:
  - `-XX:MaxRAMPercentage=75.0 -XX:+UseCompactObjectHeaders -XX:+TieredCompilation -XX:TieredStopAtLevel=1` (faster startup and lower object memory overhead in Java 25).

