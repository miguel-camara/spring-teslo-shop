# Teslo Shop API

Spring Boot port of the NestJS **Teslo RESTful API**. The same endpoints, request/response payloads, authentication flow and WebSocket behavior as the original — implemented with Spring Boot 3, Spring Security, JPA/Hibernate, JWT and Netty Socket.IO.

## Stack

| Layer        | Technology                                            |
| ------------ | ----------------------------------------------------- |
| Framework    | Spring Boot 3.5 (Java 21)                             |
| Persistence  | Spring Data JPA / Hibernate 6.6 (PostgreSQL 14)       |
| Security     | Spring Security + JWT (jjwt 0.12)                     |
| Realtime     | `netty-socketio` 2.0 (Socket.IO wire protocol)        |
| Docs         | springdoc-openapi (Swagger UI)                        |
| Build        | Maven 3.9                                            |

## Requirements

- **JDK 21+**
- **Maven 3.9+**
- **Docker** (for PostgreSQL via Docker Compose)

## Quick start

### 1. Configure the environment

Clone the template and edit as needed:

```bash
cp .env.template .env
```

| Variable      | Default                        | Description                         |
| ------------- | ------------------------------ | ----------------------------------- |
| `DB_PASSWORD` | `MySecr3tPassWord@as2`         | PostgreSQL password                 |
| `DB_NAME`     | `TesloDB`                      | Database name                       |
| `DB_HOST`     | `localhost`                    | Database host                       |
| `DB_PORT`     | `5432`                         | Database port                       |
| `DB_USERNAME` | `postgres`                     | Database user                       |
| `PORT`        | `3000`                         | HTTP server port                    |
| `HOST_API`    | `http://localhost:3000/api`    | Public API base URL                 |
| `JWT_SECRET`  | `Est3EsMISE3Dsecreto32s`       | Secret used to sign JWTs            |
| `SOCKETIO_PORT`| `3001`                        | Socket.IO server port               |
| `STAGE`       | `dev`                          | Application environment             |

### 2. Start the database

```bash
docker-compose up -d
```

### 3. Run the application

```bash
mvn spring-boot:run
```

The HTTP API starts on `http://localhost:3000/api` and the Socket.IO server on port `3001`.

### 4. Seed the database

Populates users and the product catalog (replaces existing data):

```bash
curl http://localhost:3000/api/seed
```

### 5. Open Swagger UI

```
http://localhost:3000/api/swagger-ui.html
```

> If the seed reports a duplicate-key error, the database contains stale rows from an earlier run.
> Truncate the tables and run the seed again:
> ```bash
> docker exec -it teslodb psql -U postgres -d TesloDB -c \
>   "TRUNCATE TABLE product_images, products, users RESTART IDENTITY CASCADE;"
> ```

## Demo credentials

Created by the seed, password is `Abc123` for both:

| Email             | Role       |
| ----------------- | ---------- |
| `test1@google.com`| super-user |
| `test2@google.com`| admin      |

## API reference

All endpoints are prefixed with `/api`.

### Auth (`/api/auth`)

| Method | Path            | Access     | Description                                   |
| ------ | --------------- | ---------- | --------------------------------------------- |
| POST   | `/auth/register`| Public     | Register a new user                           |
| POST   | `/auth/login`   | Public     | Login, returns a JWT                          |
| GET    | `/auth/check-status` | Authenticated | Returns current user from token            |
| GET    | `/auth/private` | Authenticated | Echoes the authenticated user + request headers |
| GET    | `/auth/private2`| `super-user`, `admin` | Role-guarded route                       |
| GET    | `/auth/private3`| `admin`     | Role-guarded route                            |

### Products (`/api/products`)

| Method | Path              | Access        | Description                          |
| ------ | ----------------- | ------------- | ------------------------------------ |
| GET    | `/products`       | Public        | Paginated list with search/sort      |
| GET    | `/products/{term}`| Public        | Find one by slug or UUID             |
| POST   | `/products`       | Authenticated | Create a product                     |
| PATCH  | `/products/{id}`  | `admin`       | Update a product                     |
| DELETE | `/products/{id}`  | `admin`       | Delete a product                     |

`GET /products` query parameters:

- `limit` — page size (default `10`, max `100`)
- `offset` — page offset (default `0`)
- `term` — search term (matches title)
- `gender` — filter by gender (`men`, `women`, `kid`, `unisex`)

### Files (`/api/files`)

| Method | Path                 | Access | Description                                   |
| ------ | -------------------- | ------ | --------------------------------------------- |
| POST   | `/files/product`     | Public | Upload a product image (`multipart/form-data`, field `file`) |
| GET    | `/files/product/{imageName}` | Public | Serve a stored product image        |

Accepted image extensions: `jpg`, `jpeg`, `png`, `gif`.

### Seed (`/api/seed`)

| Method | Path   | Access | Description                    |
| ------ | ------ | ------ | ------------------------------ |
| GET    | `/seed`| Public | Wipes and reseeds users + products |

## Authentication

1. `POST /api/auth/login` with `{ "email", "password" }` returns a JWT in the response body.
2. Send the token in the `Authorization` header:
   ```
   Authorization: Bearer <token>
   ```
3. `/api/auth/check-status` returns the currently authenticated user.

## WebSocket / Socket.IO

The Socket.IO-compatible server listens on `SOCKETIO_PORT` (default `3001`) using the same
events, handshake header (`authentication`) and payloads as the original NestJS implementation.

## Scripts

```bash
mvn spring-boot:run   # run the app
mvn package           # build the jar
mvn test              # run tests
```

## Project layout

```
src/main/java/com/teslo/shop/
├── auth/        # JWT auth, login/register, role guards
├── common/      # shared DTOs and pagination
├── config/      # security, CORS, app properties, Socket.IO
├── files/       # image upload/serving
├── products/    # product CRUD
├── seed/        # database seeding
└── TesloShopApplication.java
```

## Differences from the NestJS original

- **Socket.IO** is served by `netty-socketio` on `SOCKETIO_PORT` (`3001`) instead of sharing the HTTP port `3000`. Same events, handshake header and payloads.
- **Schema** is generated by Hibernate (`ddl-auto: update`) instead of TypeORM `synchronize`.
- **Product `price`** is stored as `double precision`; JSON output renders whole numbers without decimals to match JS serialization.
