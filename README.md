# Helpdesk API

Spring Boot REST API for support ticket management. It provides JWT authentication, role-based authorization, ticket workflows for users and staff, standardized `ProblemDetail` error responses, OpenAPI documentation, Bruno API collections, Docker Compose environments, and a local Grafana LGTM observability stack.

## Features

- User management with `ADMIN`, `TECHNICIAN`, and `USER` roles
- Email/password login with JWT bearer tokens
- User-scoped ticket creation, listing, reading, and updates
- Staff ticket triage, priority changes, technician assignment, close, and cancel workflows
- Ticket comments with participant-based access rules
- Admin-only system health and metrics facade for frontend API generation
- RFC 9457-style `ProblemDetail` error responses
- Authentication rate limiting with Bucket4j and Caffeine
- Request correlation through `X-Request-Id`, MDC logging, and optional OpenTelemetry trace IDs
- Local PostgreSQL, production Docker Compose, and Grafana LGTM observability setups

## Tech Stack

- Java 21
- Spring Boot 4.0.6
- Spring MVC, Validation, Security, OAuth2 Resource Server, Actuator, OpenTelemetry
- Spring Data JPA
- Flyway
- PostgreSQL
- Springdoc OpenAPI UI
- Bucket4j and Caffeine
- Testcontainers
- Docker Compose
- Grafana, Loki, Tempo, Mimir, and Alloy
- Bruno API collection

## Requirements

- Java 21
- Docker and Docker Compose
- A POSIX shell for the `./mvnw` examples

The Maven wrapper is included, so a separate Maven installation is not required.

## Quick Start

Create a local environment file:

```zsh
cp .env.example .env
```

Start the local PostgreSQL database:

```zsh
docker compose -f compose.dev.yaml up -d
```

Run the API with the `dev` profile:

```zsh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Useful local URLs:

| URL | Purpose |
| --- | --- |
| `http://localhost:8080/actuator/health` | API health |
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON |

## Configuration

Base configuration lives in `src/main/resources/application.properties`. Local `.env` values are loaded automatically when the file exists.

Important environment variables:

| Variable | Purpose | Dev default |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profiles | `dev` |
| `JWT_SECRET` | HS256 JWT signing secret, at least 256 bits | Set in `.env.example` |
| `JWT_EXPIRATION_MS` | JWT lifetime in milliseconds | `3600000` |
| `JWT_ISSUER` | JWT issuer claim | `helpdesk-api` |
| `DB_HOST`, `DB_PORT`, `DB_NAME` | Local database connection values | `localhost`, `5432`, `mydatabase` |
| `DB_USERNAME`, `DB_PASSWORD` | Database credentials | `myuser`, `secret` |

Profile behavior:

| Profile | Behavior |
| --- | --- |
| `dev` | Uses Hibernate `create-drop`, disables Flyway, seeds default users, exposes extra actuator loggers, and uses human-readable logs. |
| `prod` | Uses external database env vars, validates schema, requires JWT/admin setup env vars, disables Swagger, disables Spring Docker Compose integration, and emits ECS JSON logs. |
| `observability` | Used with `dev`; enables Prometheus actuator exposure, OTLP trace export, and structured logs for Loki collection. |

Production requires these additional values:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `ADMIN_NAME`
- `ADMIN_EMAIL`
- `ADMIN_PASSWORD`

## Authentication

Only `POST /api/auth/login`, health/info actuator endpoints, and Swagger/OpenAPI endpoints are public. `GET /api/auth/me` and application endpoints require a JWT bearer token.

Default `dev` users:

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@helpdesk.local` | `Admin@123456` |
| Technician | `tech@helpdesk.local` | `Tech@123456` |
| User | `user@helpdesk.local` | `User@123456` |

Get a token:

```zsh
curl -s -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@helpdesk.local","password":"Admin@123456"}'
```

Use the returned token:

```zsh
curl -s 'http://localhost:8080/api/auth/me' \
  -H 'Authorization: Bearer <jwt>'
```

Authentication requests are rate-limited by client IP to 10 requests per minute. Exceeded requests return `429 Too Many Requests` with `application/problem+json` and a `Retry-After` header.

## API Overview

All application responses are JSON. Error responses use `application/problem+json`.

### Auth

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Public | Authenticate and issue JWT |
| `GET` | `/api/auth/me` | Bearer token | Return the current authenticated user |

### Users

| Method | Path | Scope | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/users` | Admin | Create user |
| `GET` | `/api/users/{id}` | Admin, technician | Get user by ID |
| `GET` | `/api/users` | Admin | List users |
| `PATCH` | `/api/users/{id}/name` | Admin or self | Update user name |
| `PATCH` | `/api/users/{id}/role` | Admin | Change user role |
| `DELETE` | `/api/users/{id}` | Admin | Deactivate user |

### User Tickets

| Method | Path | Scope | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/users/tickets` | User | Create own ticket |
| `GET` | `/api/users/tickets` | User | List own tickets |
| `GET` | `/api/users/tickets/{ticketId}` | User | Get own ticket |
| `PUT` | `/api/users/tickets/{id}` | Authorized owner/staff | Update title and description |

### Staff Tickets

| Method | Path | Scope | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/staff/tickets` | Admin, technician | Create ticket for requester |
| `GET` | `/api/staff/tickets` | Admin, technician | List and filter all tickets |
| `GET` | `/api/staff/tickets/{ticketId}` | Admin, technician | Get ticket by ID |
| `PATCH` | `/api/staff/tickets/{ticketId}/priority` | Admin, technician | Change priority |
| `PATCH` | `/api/staff/tickets/{ticketId}/technician` | Admin, technician | Assign technician and move to `IN_PROGRESS` |
| `PATCH` | `/api/staff/tickets/{ticketId}/close` | Admin or assigned technician | Close ticket |
| `PATCH` | `/api/staff/tickets/{ticketId}/cancel` | Authorized participant/staff | Cancel ticket |

### Ticket Comments

| Method | Path | Scope | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/tickets/{ticketId}/comments` | Ticket participant | Add comment |
| `GET` | `/api/tickets/{ticketId}/comments` | Ticket participant | List comments |
| `PUT` | `/api/tickets/{ticketId}/comments/{commentId}` | Authorized commenter/staff | Update comment |
| `DELETE` | `/api/tickets/{ticketId}/comments/{commentId}` | Authorized commenter/staff | Delete comment |

### Admin System

| Method | Path | Scope | Purpose |
| --- | --- | --- | --- |
| `GET` | `/api/admin/system/health` | Admin | Stable health DTO |
| `GET` | `/api/admin/system/metrics` | Admin | List metric names |
| `GET` | `/api/admin/system/metrics/{metricName}` | Admin | Get metric details |

Use Swagger UI or the Bruno collection for complete request and response shapes.

## Ticket Rules

Ticket statuses:

- `OPEN`
- `IN_PROGRESS`
- `CLOSED`
- `CANCELLED`

Ticket priorities:

- `TRIAGE`
- `LOW`
- `MEDIUM`
- `HIGH`
- `URGENT`

Important domain rules:

- User-created tickets default to `TRIAGE` when priority is not provided.
- Updating closed or cancelled tickets is blocked.
- Assigning a technician is blocked for closed or cancelled tickets.
- Closing and cancelling tickets are guarded by entity state rules and service authorization.

## Error Handling

Global exception handlers map validation, security, web, and domain failures to `ProblemDetail` responses.

Typical fields:

```json
{
  "type": "/errors/bad-request",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/users",
  "timestamp": "2026-05-05T12:00:00Z",
  "invalid_params": []
}
```

The OpenAPI configuration also registers common reusable error responses for `400`, `401`, `403`, `404`, `409`, and `422`.

## Testing

Run unit and slice tests:

```zsh
./mvnw test
```

Run the full verification lifecycle, including integration tests:

```zsh
./mvnw verify
```

Integration tests use Testcontainers with PostgreSQL. Docker must be available for those tests; tests annotated with Testcontainers are disabled when Docker is unavailable.

## Docker

Run only the local development database:

```zsh
docker compose -f compose.dev.yaml up -d
```

Run the API and PostgreSQL with the production profile:

```zsh
docker compose -f compose.prod.yaml up -d --build
```

Compose file roles:

| File | Purpose |
| --- | --- |
| `compose.yaml` | Shared `api` and `postgres` service defaults |
| `compose.dev.yaml` | Local database and optional API profile for development |
| `compose.prod.yaml` | API and PostgreSQL using the `prod` profile |
| `compose.observability.yaml` | Local API, PostgreSQL, Grafana, Loki, Tempo, Mimir, and Alloy |

The `Dockerfile` builds a layered Spring Boot runtime image on Eclipse Temurin 21 JRE Alpine and runs as a non-root `appuser`.

## Observability

Start the API, PostgreSQL, and local LGTM stack:

```zsh
docker compose -f compose.yaml -f compose.dev.yaml -f compose.observability.yaml --profile api up -d --build
```

Services:

| URL | Purpose |
| --- | --- |
| `http://localhost:3000` | Grafana |
| `http://localhost:12345` | Alloy UI |
| `http://localhost:8080/actuator/prometheus` | Prometheus metrics |
| `http://localhost:8080/actuator/health` | API health |

The stack provisions Loki, Tempo, and Mimir datasources automatically. A starter dashboard is available under `Helpdesk / Helpdesk API`.

With the `observability` profile, the API exports OpenTelemetry traces to Alloy over OTLP HTTP. Metrics are scraped from `/actuator/prometheus`, and logs are collected from container stdout into Loki with request and trace correlation fields.

Without the `observability` profile, `/actuator/prometheus` remains protected by the same admin actuator rule as other `/actuator/**` endpoints.

## Project Structure

Package root: `src/main/java/com/thiagsilvadev/helpdesk`

| Path | Purpose |
| --- | --- |
| `api/` | OpenAPI-annotated controller interfaces |
| `controller/` | HTTP controllers |
| `service/` and `service/ticket/` | Business use cases, including ticket command/query split |
| `security/` | JWT, current-user resolution, and authorization helpers |
| `entity/` | JPA entities and domain enums |
| `repository/` | Spring Data repositories and ticket specifications |
| `dto/` | Request and response DTOs |
| `mapper/` | Entity/DTO mapping |
| `exception/handler/` | Global exception handlers |
| `filter/` | Request logging and authentication rate limiting |
| `seeder/` | Dev and production admin seeders |

Other useful paths:

| Path | Purpose |
| --- | --- |
| `src/main/resources/db/migration/` | Flyway migrations |
| `src/test/java/` | Unit, slice, and integration tests |
| `bruno/` | Bruno API collection |
| `observability/` | Grafana dashboards and LGTM component configuration |
| `docs/` | Architecture decision records |

## OpenAPI

When Swagger is enabled:

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

Swagger/OpenAPI is disabled in the `prod` profile.

## Bruno

Collection root: `bruno/`

Environment files: `bruno/environments/Local - Admin.bru`, `Local - Customer.bru`, `Local - Staff.bru`

Suggested flow:

1. Run the login request in `bruno/auth/` to populate `accessToken`.
2. Use `bruno/users/` for user management scenarios.
3. Use ticket requests by audience:
   - `bruno/tickets/user/`
   - `bruno/tickets/staff/`
   - `bruno/tickets/comments/`
4. Use `bruno/exceptions/` to validate error scenarios.

## License

MIT. See `LICENSE.md`.
