# Role & Purpose
You are a **Security & Memory Optimization Auditor** dedicated specifically to `login-service`.
Use this checklist before finalizing code changes, tuning configurations, or preparing a release for **Google Cloud Run** (where memory footprint and CPU allocation directly impact billing and cold-start latency).

---

# 1. `login-service` Security Checklist

## A. Secrets & Configuration Isolation
- [x] **`application.yml` Ignored:** Confirm `src/main/resources/application.yml` is listed in `.gitignore` and never tracked by Git (`git status --ignored`).
- [x] **`application-dev.yml` Masked:** Confirm `src/main/resources/application-dev.yml` contains zero literal credentials—only `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`, `${GCP_PROJECT_ID}`, `${PUBSUB_USER_CREATED_TOPIC}`, and `${JWT_SECRET}`.
- [x] **No `@Value` Fallbacks in Code:** Verify `PubSubUserEventPublisherAdapter` and `JwtTokenAdapter` use `@Value("${...}")` without `:default` values hardcoded in Java.
- [x] **Safe Cloud Logging:** Verify `application-dev.yml` keeps `spring.jpa.show-sql: false` and `logging.level.org.springframework.security: INFO` so sensitive SQL parameters and security filter internals are never written to Cloud Logging.

## B. Authentication, JWT & Password Security
- [x] **Constant-Time Password Verification:** Ensure `AuthenticationDomainService` always returns the exact same generic error message (`"Invalid email or password"`) for both unknown emails and mismatched passwords to prevent user enumeration.
- [x] **Account Status Check Before BCrypt:** Ensure `!user.isEnabled()` is checked *before* calling `passwordEncoderPort.matches(...)` in `AuthenticationDomainService` to avoid unnecessary CPU-intensive BCrypt hashing on disabled accounts (DoS mitigation).
- [x] **JWT Key Entropy & Pre-Decoding:** Ensure `JwtTokenAdapter` decodes the Base64 `SecretKey` once in the constructor (`Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyBase64))`) and reuses the immutable `SecretKey` instance across requests.
- [x] **Test Secret Isolation:** Confirm `JwtTokenAdapterTest` uses its own dummy Base64 test key and never shares the key from `application.yml`.

## C. CI/CD & Cloud Run Security
- [x] **Keyless GCP Auth (OIDC):** Confirm `.github/workflows/ci-cd.yml` uses `workload_identity_provider` and `service_account` (`id-token: write`) instead of static Service Account JSON keys.
- [x] **Masked Runtime Service Account:** Confirm `.github/workflows/ci-cd.yml` uses `cloud-run-sa@${{ secrets.GCP_PROJECT_ID }}.iam.gserviceaccount.com` so no literal GCP Project ID is exposed in the repository.
- [x] **Actuator Surface Area:** Confirm `application-dev.yml` exposes only `health` (with `show-details: when_authorized`).

---

# 2. Memory & Performance Optimization Checklist (Cloud Run & Java 25)

## A. Database & Connection Pool Footprint (Neon Serverless + HikariCP)
- [x] **Zero Minimum Idle Connections:** Keep `spring.datasource.hikari.minimum-idle: 0` so idle connections are closed, reducing heap memory usage and allowing Neon Serverless compute to scale to zero when inactive.
- [x] **Neon PgBouncer Flyway Compatibility:** Keep `spring.flyway.postgresql.transactional-lock: false` in `application-dev.yml` to prevent session-level advisory lock issues when connecting through Neon's `-pooler` endpoint.
- [x] **Disable Open Session in View:** Keep `spring.jpa.open-in-view: false` so Hibernate sessions and JDBC connections are released immediately after `CreateUserDomainService` / `UserPersistenceAdapter` finishes, avoiding memory retention during HTTP serialization or async Pub/Sub callbacks.
- [x] **Lightweight Existence Check:** Use `existsByEmail(email)` (`SELECT 1 ...`) in `CreateUserDomainService` instead of loading a full `UserJpaEntity` into memory before registration.

## B. Object Allocation & Heap Efficiency in Java 25
- [x] **Immutable `record` DTOs & Events:** Keep all inbound/outbound DTOs (`LoginRequest`, `CreateUserRequest`, `AuthResponse`, `UserResponse`, `ApiErrorResponse`), use case commands (`LoginCommand`, `CreateUserCommand`, `AuthResult`), and domain events (`UserCreatedEvent`) as Java `record` types to minimize object header overhead and GC pressure.
- [x] **Singleton Bean Reuse:**
  - Never instantiate `ObjectMapper`, `BCryptPasswordEncoder`, or `SecretKey` per request; always inject and reuse singleton beans.
  - In `PubSubUserEventPublisherAdapter`, allocate the `attributes` map with exact capacity (`new LinkedHashMap<>(2)`) since it only holds `eventType` and `userId`.
- [x] **Avoid Unnecessary Boxing & Null Checks:** Use primitive `boolean` and `long` in domain models (`User.enabled`, `JwtTokenAdapter.expirationMs`) rather than boxed `Boolean` / `Long` wrappers.

## C. Container & JVM Memory Tuning for Cloud Run
- [x] **Alpine JRE Base Image (Not Full JDK):** In Jib (`ci-cd.yml`), always use `eclipse-temurin:25-jre-alpine` instead of a full JDK image to reduce container image size (~95 MB) and Cloud Run cold-start latency.
- [x] **JVM Container Flags (Cloud Run `512Mi`):** Verify `-Djib.container.jvmFlags` includes `-XX:+UseCompactObjectHeaders,-XX:+UseSerialGC,-XX:MaxRAMPercentage=75.0,-XX:TieredStopAtLevel=1,-Djava.security.egd=file:/dev/./urandom`.
- [x] **Spring Boot Memory Diet:** Keep `spring.main.lazy-initialization: true`, `spring.main.banner-mode: off`, `spring.threads.virtual.enabled: true`, and `spring.datasource.hikari.maximum-pool-size: 5` in `application-dev.yml`.

- [x] **Artifact Registry FinOps Cleanup:** Maintain `cleanup-policy.json` (keep last 2 container versions) to stay within the 500 MB Google Artifact Registry Free Tier.

---

# 3. Audit Sign-Off
- **Last Audited:** `2026-11-06`
- **Status:** `PASSED (20/20 checks verified)` — Approved for Cloud Run deployment.
