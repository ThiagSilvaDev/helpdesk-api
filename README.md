# Helpdesk API

A Spring Boot 4 REST API for support ticket management, with JWT authentication, role-based authorization, ProblemDetail error responses, and a CQRS-style split for ticket use cases.

## What This Project Does

- Manages users (`ADMIN`, `TECHNICIAN`, `USER`)
- Authenticates with email/password and JWT
- Allows users to create and manage their own tickets
- Allows staff to triage and operate on all tickets
- Returns standardized error payloads (RFC 9457-style `ProblemDetail`)

## Tech Stack

- Java 21
- Spring Boot 4.0.3
- Spring MVC + Validation
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL (runtime)
- Springdoc OpenAPI UI
- Spring Actuator
- Bruno collection for API testing

## Architecture (Current)

Package root: `src/main/java/com/thiagsilvadev/helpdesk`

- `controller/` HTTP layer
  - `AuthController`
  - `UserController`
  - `UserTicketController`
  - `StaffTicketController`
- `service/` and `service/ticket/`
  - `AuthService`, `UserService`
  - `TicketCommandService` (writes)
  - `TicketQueryService` (reads)
- `security/`
  - `WebSecurityConfig`, JWT filter/service, custom user details
  - Method-level authorization (`@PreAuthorize`) with `userAuthorization` and `ticketAuthorization`
- `entity/` domain model (`User`, `Ticket`, enums)
- `repository/` JPA repositories + specifications
- `exception/handler/` global exception mapping to `ProblemDetail`
- `mapper/` entity <-> DTO mapping

## Security Model

- JWT auth for all endpoints except:
  - `/api/auth/**`
  - `/actuator/health`, `/actuator/info`
  - Swagger endpoints (`/v3/api-docs/**`, `/swagger-ui/**`)
- Method security is enabled (`@EnableMethodSecurity`)
- Authorization split by role and ownership:
  - User endpoints are ownership-oriented (self scope)
  - Staff endpoints are role-oriented (`ADMIN`/`TECHNICIAN`)
  - Some operations are shared and finalized by `@PreAuthorize` rules in services

## Main Endpoints

### Auth

- `POST /api/auth/login`

### Users (`/api/users`)

- `POST /api/users` - create a user (admin scope)
- `GET /api/users/{id}` - get a user
- `GET /api/users` - list users
- `PUT /api/users/{id}` - update a user
- `DELETE /api/users/{id}` - deactivate a user

### User Tickets (`/api/users/tickets`)

- `POST /api/users/tickets` - create a ticket as the authenticated user
- `GET /api/users/tickets/{ticketId}` - get own ticket by ID
- `GET /api/users/tickets` - list own tickets (paged)
- `PUT /api/users/tickets/{id}` - update ticket (owner or authorized staff, per service authorization)

### Staff Tickets (`/api/staff/tickets`)

- `POST /api/staff/tickets` - create a ticket for a requester (`requesterId`, `priority`)
- `GET /api/staff/tickets/{ticketId}` - get a ticket
- `GET /api/staff/tickets` - list tickets with filters (paged)
- `PATCH /api/staff/tickets/{ticketId}/priority`
- `PATCH /api/staff/tickets/{ticketId}/technician`
- `PATCH /api/staff/tickets/{ticketId}/close`
- `PATCH /api/staff/tickets/{ticketId}/cancel`

## Ticket Domain Rules (Highlights)

- Status enum: `OPEN`, `IN_PROGRESS`, `CLOSED`, `CANCELLED`
- Priority enum: `TRIAGE`, `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- New ticket defaults to `TRIAGE` when priority is not provided
- Guarded transitions implemented in `Ticket` entity methods:
  - update blocked for closed/cancelled tickets
  - assign technician blocked for closed/cancelled tickets
  - close/cancel enforce state rules

## Error Response Format

Global handlers return `ProblemDetail` with additional metadata.

Typical fields:

- `type` (e.g. `/errors/bad-request`)
- `title`
- `status`
- `detail`
- `instance`
- `timestamp`
- optional `invalid_params` (validation errors)

## Profiles and Configuration

Base: `src/main/resources/application.properties`

- JWT:
  - `security.jwt.secret`
  - `security.jwt.expiration-ms`
- Actuator base path: `/actuator`

`dev` profile (`application-dev.properties`):

- Seeds default users via `DevAdminSeederConfig`:
  - admin: `admin@helpdesk.local`
  - user: `user@helpdesk.local`
  - technician: `tech@helpdesk.local`

### Get Dev JWT Tokens (curl)

Use the default dev credentials from `application-dev.properties` to get JWTs from `POST /api/auth/login`.

Admin token:

```zsh
curl -s -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@helpdesk.local","password":"Admin@123456"}'
```

Technician token:

```zsh
curl -s -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email":"tech@helpdesk.local","password":"Tech@123456"}'
```

User token:

```zsh
curl -s -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@helpdesk.local","password":"User@123456"}'
```

Each response returns JSON in the format:

```json
{"token":"<jwt>"}
```

`prod` profile (`application-prod.properties`):

- External DB via env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- JWT secret required from env
- Swagger disabled
- Structured ECS JSON logs to stdout via Spring Boot's default Logback backend

## Logging

- Spring Boot's built-in Logback setup is used; no separate logging framework is added
- Every request gets an `X-Request-Id` header and the same value is added to the logging MDC as `requestId`
- Local and non-`prod` profiles keep the default human-readable console output
- `prod` emits structured JSON logs to stdout, which fits container log collectors better than local file rotation

## Running Locally

### 1) Start PostgreSQL for local development (optional, recommended)

```zsh
docker compose -f compose.dev.yaml up -d
```

This starts the local development database only. The `dev` profile is configured to use `compose.dev.yaml` and connects to `localhost:5432` by default.
`compose.dev.yaml` inherits shared settings from `compose.yaml`.

### 2) Run the API

```zsh
./mvnw spring-boot:run
```

To run with dev profile:

```zsh
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Containerized Production Stack

To run the API and PostgreSQL together with the `prod` profile:

```zsh
docker compose -f compose.prod.yaml up -d --build
```

`compose.prod.yaml` also inherits shared service defaults from `compose.yaml`.

## API Testing with Bruno

Collection root: `bruno/`

Suggested order:

1. Use `bruno/auth/` login requests to populate tokens (`adminAccessToken`, `userAccessToken`, `technicianAccessToken`)
2. Use `bruno/users/` to create/read users
3. Use ticket requests by audience:
   - `bruno/tickets/user/`
   - `bruno/tickets/staff/`
4. Use `bruno/exceptions/` to validate error scenarios

Environment file: `bruno/environments/local.bru`

## OpenAPI

When enabled, use:

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

## License

See `LICENSE.md`.
