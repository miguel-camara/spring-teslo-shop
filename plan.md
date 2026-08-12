# Plan: Recreate the Porfolio Web API in Spring Boot

This document analyzes the existing NestJS + TypeORM implementation (`portfolio`) and provides a step-by-step plan to recreate the **same REST API** in **Java / Spring Boot**, preserving endpoints, payloads, auth flow, and Docker setup.

---

## 1. Overview of the Original API

A small e-commerce backend exposing a REST API under the `/api` prefix.

| Feature            | Original (NestJS)                            | Target (Spring Boot)                              |
| ------------------ | -------------------------------------------- | ------------------------------------------------- |
| Language / runtime | TypeScript / Node 22                         | Java 17+                                          |
| Web framework      | NestJS 10 + Express                          | Spring Web (MVC)                                  |
| ORM                | TypeORM                                      | Spring Data JPA (Hibernate)                       |
| Database           | PostgreSQL 14.3                              | PostgreSQL 14.3                                   |
| Validation         | `class-validator` + global `ValidationPipe`  | Jakarta Bean Validation (`@Valid`)                |
| Auth               | `@nestjs/jwt` + passport-jwt + custom guards | Spring Security + `jjwt`                          |
| Password hashing   | `bcrypt`                                     | Spring Security `BCryptPasswordEncoder`           |
| File upload        | Multer (disk storage)                        | Spring `MultipartFile`                            |
| Static files       | `ServeStaticModule`                          | Resource handler / `WebMvcConfigurer`             |
| Docs               | Swagger (`@nestjs/swagger`)                  | `springdoc-openapi`                               |
| Config             | `@nestjs/config` + `.env`                    | Spring profiles + `.env` (or `SPRING_*` env vars) |
| Container          | `docker-compose` (Postgres only)             | Same `docker-compose`                             |
| Tests              | Jest + Supertest                             | JUnit 5 + MockMvc                                 |

### Global behaviors to replicate

- Global URL prefix `/api`.
- CORS enabled for all origins.
- Global validation: unknown body fields are **rejected** (`forbidNonWhitelisted: true`, `whitelist: true`) — use `spring.jackson`/DTOs that ignore unknown or fail on unknown fields; recommended to reject unknown properties via `@JsonIgnoreProperties(ignoreUnknown = false)` or a custom `@RestControllerAdvice`.
- Swagger UI mounted at `/api`.

---

## 2. Endpoint Contract (must match exactly)

### Auth — `/api/auth`

| Method | Path          | Auth | Request body          | Response                |
| ------ | ------------- | ---- | --------------------- | ----------------------- |
| POST   | `/auth/login` | none | `{ email, password }` | `{ user, token }` (200) |

- **User JSON shape:** `{ id, email }` — `password` is NEVER serialized.
- **Token:** JWT HS256, payload `{ id }`, expiry **2 hours**, secret from env `JWT_SECRET`.
- **register**: email normalized to lowercase+trim; password hashed with bcrypt cost 10; returns `{ user, token }`.
- **login**: email lookup with password fetched explicitly; on bad email → `401` "Credentials are not valid (email)"; on bad password → `401` "Credentials are not valid (password)".
- **check-status**: re-issues a fresh token for the authenticated user.

### Projects — `/api/projects`

| Method | Path            | Auth                  | Body / Params                | Response                       |
| ------ | --------------- | --------------------- | ---------------------------- | ------------------------------ |
| POST   | `/projects`     | Bearer JWT (any role) | `CreateProjectDto`           | Project (201)                  |
| GET    | `/projects`     | none                  | `?limit=10&offset=0`         | `{ count, pages, projects[] }` |
| PATCH  | `/projects/:id` | Bearer JWT + `admin`  | `UpdateProjectDto` (partial) | Project (plain)                |
| DELETE | `/projects/:id` | Bearer JWT + `admin`  | —                            | 200/204                        |

- **Product JSON (plain form):** `{ id, title, price, description, slug, stock, sizes[], gender, tags[], images: [url...] }` — images flattened to an array of URL strings (not objects). Created/updated responses also include `user`.
- **Pagination:** `limit` default 10, `offset` default 0; `gender` filter matches `gender = :g OR gender = 'unisex'`; ordered by `id ASC`; `count` = total matches; `pages = ceil(count / limit)`.
- **Validation (create):**
  - `title`: string, min 1 (unique)
  - `price`: number > 0, optional
  - `description`: string, optional
  - `slug`: string, optional
  - `stock`: int > 0, optional
  - `sizes`: string array (required)
  - `gender`: one of `men|women|kid|unisex` (required)
  - `tags`: string array, optional
  - `images`: string array, optional
- **Update:** partial (all fields optional); if `images` present, they REPLACE the existing set inside a transaction.
- **DB errors:** unique violation (`23505`) → `400` with DB detail message.

### Files — `/api/files`

| Method | Path                        | Auth | Response                                           |
| ------ | --------------------------- | ---- | -------------------------------------------------- |
| GET    | `/files/project/:imageName` | none | image file from `static/projects`                  |
| POST   | `/files/project`            | none | multipart field `file` → `{ secureUrl, fileName }` |
| GET    | `/files/pdf/:pdfName`       | none | image file from `static/pdf`                       |
| POST   | `/files/pdf`                | none | multipart field `file` → `{ secureUrl, fileName }` |

- Upload: `Content-Type: multipart/form-data`, field name `file`.
- Allowed extensions: `jpg, jpeg, png, webp, pdf`.
- Stored name: `{uuid}.{extension}` (random UUID v4 + original mimetype extension).
- Saved to `./static/products` and `./static/pdf`.
- Response: `{ secureUrl: `${HOST_API}/files/project/{name}`, fileName: name }`.
- Serving: returns 400 "No product found with image {imageName}" if missing.

## 3. Data Model (JPA Entities)

Table/column mapping is defined by TypeORM. Hibernate schema can be generated via `spring.jpa.hibernate.ddl-auto=create-drop` (mirrors `synchronize: true`) but recommend Flyway for prod parity.

### `users`

| Column     | Type                   | Notes                                       |
| ---------- | ---------------------- | ------------------------------------------- |
| `id`       | UUID PK                | `@GeneratedValue(strategy = UUID)` / `uuid` |
| `email`    | text, unique, not null | normalized lowercase+trim                   |
| `password` | text, not null         | bcrypt hash; serialization-excluded         |
| `fullName` | text, not null         |                                             |
| `product`  | one-to-many            | products owned by the user                  |

### `products`

| Column        | Type                         | Notes                  |
| ------------- | ---------------------------- | ---------------------- |
| `id`          | UUID PK                      |                        |
| `title`       | text, unique, not null       |                        |
| `price`       | float, default `0`           |                        |
| `description` | text, nullable               |                        |
| `slug`        | text, unique, not null       | auto-generated         |
| `stock`       | int, default `0`             |                        |
| `sizes`       | text array                   |                        |
| `gender`      | text                         | `men/women/kid/unisex` |
| `tags`        | text array, default `[]`     |                        |
| `user`        | many-to-one → users          | eager fetch            |
| `images`      | one-to-many → product_images | cascade, eager fetch   |

### `product_images`

| Column    | Type                   | Notes               |
| --------- | ---------------------- | ------------------- |
| `id`      | serial PK (int)        |                     |
| `url`     | text, not null         |                     |
| `project` | many-to-one → projects | `on delete cascade` |

**Notes for Hibernate:**

- Use Hibernate 6 native `String[]`/`List<String>` support for text arrays (PostgreSQL `@JdbcTypeCode(SqlTypes.ARRAY)`).
- Match `eager` joins explicitly (JPQL `fetch` or `@EntityGraph`) to avoid `LazyInitializationException`; simplest is to fetch images/user eagerly with `@ManyToOne(fetch = EAGER)` + `@OneToMany(fetch = EAGER)` like TypeORM, or use DTO projections.
- Password field: `@JsonProperty(access = WRITE_ONLY)` + strip in the mapper.

---

## 4. Project Structure (Maven/Gradle)

```
teslo-shop-spring/
├── docker-compose.yaml          # same as original (Postgres only)
├── .env / .env.template
├── pom.xml or build.gradle
└── src/main/
    ├── java/com/example/teslo/
    │   ├── TesloApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java        # filter chain, CORS
    │   │   ├── OpenApiConfig.java         # springdoc config + bearer scheme
    │   │   ├── WebConfig.java             # static resource handler
    │   │   └── JwtAuthFilter.java         # OncePerRequestFilter
    │   ├── auth/
    │   │   ├── controller/AuthController.java
    │   │   ├── service/AuthService.java
    │   │   ├── dto/{RegisterRequest, LoginRequest, AuthResponse, UserResponse}.java
    │   │   ├── entity/User.java
    │   │   ├── repository/UserRepository.java
    │   │   └── jwt/{JwtService, JwtProperties}.java
    │   ├── products/
    │   │   ├── controller/ProductsController.java
    │   │   ├── service/ProductsService.java
    │   │   ├── dto/{CreateProductRequest, UpdateProductRequest, ProductResponse, PaginationResponse}.java
    │   │   ├── entity/{Product, ProductImage}.java
    │   │   └── repository/{ProductRepository, ProductImageRepository}.java
    │   ├── files/
    │   │   ├── controller/FilesController.java
    │   │   └── service/FilesService.java
    │   ├── seed/
    │   │   ├── controller/SeedController.java
    │   │   └── service/SeedService.java     # data loader (JSON or resource file)
    │   ├── messagesws/
    │   │   ├── MessagesWsConfig.java        # WebSocket + interceptor
    │   │   └── MessagesWsService.java       # connected-client registry
    │   └── common/
    │       ├── exception/GlobalExceptionHandler.java  # @RestControllerAdvice
    │       └── dto/PaginationRequest.java
    └── resources/
        ├── application.yml
        ├── application-prod.yml
        └── seed-data.json                  # port of src/seed/data/seed-data.ts
```

---

## 5. Configuration

### `application.yml` (env-driven)

```yaml
server:
  port: ${PORT:8080}
  servlet:
    context-path: /api # global /api prefix (equivalent to setGlobalPrefix)

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:Portfolio}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:MySecr3tPassWord@as2}
  jpa:
    hibernate:
      ddl-auto: create-drop # mirrors synchronize:true (dev); use Flyway in prod
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    open-in-view: false
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB

app:
  host-api: ${HOST_API:http://localhost:8080/api}
  jwt:
    secret: ${JWT_SECRET:Est3EsMISE3Dsecreto32s}
    expiration: 7200 # 2 hours in seconds
  stage: ${STAGE:dev}
  upload-dir: /static/products
  upload-dir-pdf: /static/pdf
```

### Env var mapping (`.env`)

`STAGE`, `DB_PASSWORD`, `DB_NAME`, `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `PORT`, `HOST_API`, `JWT_SECRET` — same names as the original. `docker-compose.yaml` is reused as-is.

---

## 6. Authentication (Spring Security)

1. **`JwtAuthFilter`** (extends `OncePerRequestFilter`):
   - Reads `Authorization: Bearer <token>`.
   - Verifies signature/expiry with `JwtService` (jjwt).
   - Loads `User` by the `id` claim; rejects if user missing or `!isActive` (mirrors `jwt.strategy.ts`).
   - Populates `SecurityContext` with `{ id, roles, fullName }` as the principal.
2. **`SecurityConfig`**:
   - `csrf().disable()`, `cors()` enabled for all.
   - Public: `/auth/login`, `/projects/**` (GET), `/files/**`, `/api-docs/**`, `/swagger-ui/**`, static resources.
   - Authenticated: `/auth/**` GETs, `POST /products`.
   - `BCryptPasswordEncoder` bean.

---

## 7. Controllers (key implementation hints)

**AuthController** — map exactly to the table in §2. Return `UserResponse` (never password). `check-status` re-issues token.

**ProductsController**

```java
@GetMapping
public PaginationResponse findAll(@Valid PaginationRequest p)   // GET /api/products?limit&offset&gender

@GetMapping("/{id}")
public ProductResponse findOne(@PathVariable UUID id)

@PostMapping
public ProductResponse create(@RequestBody @Valid CreateProductRequest body, @AuthenticationPrincipal User user)

@PatchMapping("/{id}")
public ProductResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateProductRequest body, @AuthenticationPrincipal User user)

@DeleteMapping("/{id}")
public ResponseEntity<Void> remove(@PathVariable UUID id)
```

- `findAll`: `WHERE (:gender = '' OR gender = :gender OR gender = 'unisex')` with `limit`/`offset` and `ORDER BY id ASC`; return `count`, `pages = ceil(count/limit)`, flattened products.
- `update`: load + partial-apply non-null fields; if `images != null`, delete existing `ProductImage` rows and re-insert (transactional).
- Slug and email normalization via `@PrePersist`/`@PreUpdate` (mirrors `BeforeInsert`/`BeforeUpdate`).
- `remove`: load or 404, then delete (cascade removes images).

**FilesController**

- `GET /files/product/{imageName}` → return `Resource` from `static/products` (or throw 400 if missing).
- `POST /files/product` → `MultipartFile file`; validate extension in `{jpg,jpeg,png,webp}`; save as `UUID.randomUUID().toString() + "." + ext`; return `{ secureUrl: hostApi + "/files/product/" + name, fileName: name }`.

---

## 8. WebSocket Parity

Spring's socket.io compatibility is imperfect; two options:

1. **Recommended (byte-compatible):** Add the `netty-socketio` library (`com.corundumstudio.socketio`). It supports the same wire protocol and custom `authentication` handshake header. Register a listener for `connect`/`disconnect` and `message-from-client`, emit `clients-updated` and `message-from-server`. Keep a `ConcurrentHashMap<String, Client>` registry and disconnect the previous socket when the same `userId` reconnects.
2. **Alternative (native STOMP):** Use Spring WebSocket + STOMP. Endpoint differs from Socket.IO clients, so frontends would need changes. Only choose if the client can switch protocols.

Behavior checklist (from `messages-ws.gateway.ts`):

- Verify JWT from `authentication` header; else `client.disconnect()`.
- On connect: register, emit `clients-updated` (array of socket ids) to all.
- On disconnect: unregister, emit `clients-updated`.
- `message-from-client` `{message}` → broadcast `message-from-server` `{fullName, message}` to all connected clients.
- Validate `message` min length 1.

---

## 9. Docker & Deployment

- Reuse `docker-compose.yaml` (Postgres 14.3, port 5432) exactly as-is.
- Optionally add a `Dockerfile` for the Spring Boot app (multi-stage build → `openjdk:17-jre-slim`) and a `spring` service in compose, plus a Postgres volume for persistence.
- For prod parity: set `STAGE=prod`, enable SSL if the managed Postgres requires it (JDBC URL param `ssl=true&sslmode=require`).

---

## 10. Error Handling Parity

Original NestJS global `ValidationPipe` + services throwing `BadRequestException`/`NotFoundException`/`UnauthorizedException`/`ForbiddenException`. Implement a `@RestControllerAdvice` mapping:

| NestJS exception                                   | HTTP | Spring mapping                                                  |
| -------------------------------------------------- | ---- | --------------------------------------------------------------- |
| `BadRequestException` (validation / unique / file) | 400  | `MethodArgumentNotValidException`, custom `BadRequestException` |
| `UnauthorizedException`                            | 401  | `AuthenticationException`                                       |
| `ForbiddenException`                               | 403  | `AccessDeniedException` (custom message with required roles)    |
| `NotFoundException`                                | 404  | custom `NotFoundException` / `ResponseStatusException`          |
| `InternalServerErrorException`                     | 500  | catch-all                                                       |

- Validation errors: original returns one `message` per field (class-validator array). Match shape with a `FieldError` list (`{ field, message }`) or a single message string.
- Unknown body fields must fail: `@JsonIgnoreProperties(ignoreUnknown = false)` or a global `ObjectMapper` with `FAIL_ON_UNKNOWN_PROPERTIES` overridden to reject.

---

## 11. Testing Strategy

| Layer       | Tool                                        | Scope                                                                                            |
| ----------- | ------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| Unit        | JUnit 5 + Mockito                           | AuthService, ProductsService (find by term, pagination, image replace), slug/email normalization |
| Web         | MockMvc (or `@WebMvcTest`)                  | Controllers: status codes, validation failures, role guards, serialization (no password in body) |
| Integration | `@SpringBootTest` + Testcontainers Postgres | Full CRUD flows, JWT round-trip, seed execution                                                  |
| E2E         | REST Assured / HttpWebRequest               | Match original endpoints & payloads against a running instance                                   |

Port the `static/products` images folder and `seed-data.ts` → `seed-data.json` verbatim so the seed produces the identical catalog.

---

## 12. Implementation Order (checklist)

1. Scaffold Spring Boot project (Spring Initializr: `Web`, `Data JPA`, `PostgreSQL`, `Validation`, `Security`, `springdoc-openapi`). Add jjwt deps.
2. `application.yml` + `.env` + reuse `docker-compose.yaml`.
3. Entities (`User`, `Proyect`, `ProyectImage`, , `ListPdf`) + repositories + `@PrePersist`/`@PreUpdate` hooks.
4. Global exception handler + validation config (reject unknown fields).
5. JWT: `JwtService`, `JwtAuthFilter`, `SecurityConfig`, CORS.
6. Auth controller/service (register, login, check-status, private routes with roles).
7. Products controller/service (create/findAll/findOne/update/remove + pagination + term search + image replacement in transaction).
8. Files controller/service (upload + serve static).
9. . WebSocket gateway with JWT handshake + connected-client registry.
10. . Swagger/OpenAPI config with bearer auth; verify UI at `/api/swagger-ui`.
11. Tests (unit → integration → e2e).
12. Dockerize the app; verify full stack with `docker compose up`.
13. Manual parity pass: run both versions, diff every endpoint's status + body against the tables in §2.

<!-- Entity Project - Crud -->

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] tags = new String[0];

    @Column(nullable = true)
    private String url;

    @Column(nullable = true)
    private String repoFrontend;

    @Column(nullable = true)
    private String repoBackend;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<ProjectImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;


    <!-- Entity ListPdf independent CRUD / Mulitpart -->
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String url;

funcionalidades

.env
properties - dev (h2 db)
properties - prod

// rutas
/
/auth -> solo login
/products -> CRUD de productos
/files -> CRUD de archivos
