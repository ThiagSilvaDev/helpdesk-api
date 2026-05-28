# Repository Guidelines

## Project Structure & Module Organization

This is a Java 21 Spring Boot API. Application code lives under `src/main/java/com/thiagsilvadev/helpdesk`, organized primarily by responsibility, with feature grouping preferred as modules grow: `controller`, `service`, `repository`, `entity`, `dto`, `security`, `config`, `exception`, `messaging`, and `api`. The `api` package contains OpenAPI interfaces/contracts and external request-response models.

Tests mirror the same package layout in `src/test/java`, with integration tests in `src/test/java/com/thiagsilvadev/helpdesk/integration`. Runtime configuration is in `src/main/resources`; Flyway migrations are in `src/main/resources/db/migration`. Bruno API collections are in `bruno/`, architecture notes in `docs/`, Docker Compose files at the root, and Grafana/Loki/Tempo/Mimir configuration in `observability/`.

## Build, Test, and Development Commands

* `cp .env.example .env`: create local environment settings.
* `docker compose -f compose.dev.yaml up -d`: start local dependencies such as PostgreSQL.
* `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`: run the API locally with the `dev` profile.
* `./mvnw test`: run unit and slice tests through Surefire.
* `./mvnw verify`: run the full Maven verification lifecycle, including Failsafe integration tests.
* `docker compose -f compose.observability.yaml up -d`: start the local observability stack when needed.

## Coding Style & Naming Conventions

Use Java 21 and standard Spring conventions. Prefer constructor injection and keep controllers thin.

Controllers must remain thin and should not contain persistence or business logic. Services orchestrate application workflows and transaction boundaries. Repositories remain persistence-focused. Prefer a rich domain model where entities encapsulate domain state, invariants, and business behavior instead of acting as anemic data containers.

DTOs should define API payloads rather than exposing entities directly. Prefer manual mapping or dedicated mapper classes instead of exposing JPA entities through the API.

## Testing Guidelines

The project uses JUnit Jupiter, Spring Boot Test, Spring Security Test, Mockito, and Testcontainers. Name unit and slice tests `*Test`; name integration tests `*IT` so Failsafe runs them during `./mvnw verify`.

Prefer focused service/controller tests for business rules and use integration tests for cross-layer workflows, persistence, security, and container-backed behavior.

Test configuration belongs in `src/test/resources/application-test.properties`.

## Database & Migration Guidelines

Database schema changes must always include a Flyway migration. Never rely on Hibernate auto-DDL in production environments.

Prefer explicit schema evolution through versioned migrations stored in `src/main/resources/db/migration`.

## Commit Messages

All commit messages MUST follow Conventional Commits.

Use this format:

`type(scope): description`

Scope is optional. If omitted:

`type: description`

### Rules

* Use scope only when the change is strongly localized to a specific domain.
* Use the imperative mood.
* Start the description with a lowercase letter.
* Omit the trailing period.
* Keep the first line at 50 characters or less.

### Allowed Types

`feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`, `perf`, `style`.

### Breaking Changes

When a change breaks compatibility, use `!` before the colon:

`feat(user)!: remove legacy auth`

Or use a `BREAKING CHANGE:` footer.

## Security & Configuration Tips

Do not commit real secrets from `.env`; update `.env.example` when adding required settings.

Production-sensitive values include `JWT_SECRET`, database credentials, and admin bootstrap credentials.

Keep profile-specific behavior in `application-dev.properties`, `application-prod.properties`, or `application-observability.properties`.

## Things to Avoid

* Do not expose JPA entities directly in controllers.
* Do not place business rules inside controllers.
* Do not introduce shared utilities unless they are truly cross-cutting.
* Do not use field injection; prefer constructor injection.
* Do not bypass Flyway migrations with manual database changes.
