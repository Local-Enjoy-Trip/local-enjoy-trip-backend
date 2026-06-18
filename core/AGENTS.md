# Core Module Coding Style & Rules

## Target Shape Note

`core/core-api` is a deliberate monolithic migration target and has its own nearest
`core/core-api/AGENTS.md`. For files under `core-api`, follow that file first. The pure-domain guidance below
still applies to `com.ssafy.enjoytrip.core.domain.*` code inside `core-api`, but it does not forbid the target module's
web, worker, storage-db-repository imports, external-client, or support packages.

## Stop First: Validation Boundary

Before adding validation, parsing, defaulting, fallback, or defensive null/blank handling in `core`, stop and check the
existing ingress boundary first.

- Put HTTP query/body shape validation, request DTO `null`/blank/range checks, raw string parsing, trimming/normalizing,
  and default-value conversion in `core-api` web DTO or `batch` ingress code, not in domain services.
- `core` validation is allowed only for domain invariants, authenticated user ownership, persisted state, or
  cross-resource business rules.
- If existing request DTO validation already guarantees a value, do not repeat the same `isBlank`, `trim/strip`,
  defaulting, or fail-fast branch in the service.
- Do not add storage pre-check queries merely to validate request shape when DB constraints or existing domain flow
  already express the failure.
- Do not catch storage, external, or runtime exceptions in `core` to return success-shaped fallback data unless
  that fallback is an explicit product contract and the failure remains observable.

## Operating Principles

- **Spring Boot Native**: The `core` module is built to be a Spring Boot native module. It may use Spring stereotype
  annotations such as `@Service` and `@Component` to support Auto-Configuration and Component Scanning in consuming modules.
- **Record First**: Use Java `record` for DTOs and simple domain models that don't require complex state management.
- **Lombok Usage**: Use Lombok to reduce boilerplate code, especially for dependency injection and logging.
- **Final Fields**: Always use `final` for fields that are injected via constructor to ensure immutability and thread safety.

## Coding Style

- **Dependency Injection**: Use `@RequiredArgsConstructor` for constructor injection of `final` fields. Avoid manual constructors when possible.
- **Naming Conventions**:
  - Services should end with `Service`.
  - Repositories should end with `Repository` (interfaces).
  - Domain objects should be clear and descriptive (e.g., `Member`, `Attraction`).
- **Error Handling**: Use custom exceptions defined in `com.ssafy.enjoytrip.exception` to represent business errors.
- **Immutability**: Prefer immutable domain objects. For `record` types, use "wither" style methods (e.g., `withPassword`) to return a new instance with updated values.
- **Validation Ownership**: Keep validation in `core` only when it protects domain invariants, authenticated user ownership,
  persisted state, or cross-resource business rules. Do not put HTTP query/body shape checks, raw string parsing,
  request DTO null/blank/range checks, or batch job parameter presence checks in `core` services when `app` or `batch`
  can validate and normalize them at the ingress boundary.
- **Command Boundary**: Service commands passed into `core` should already express the intended business operation. Prefer
  normalized command/value objects over repeated `null`, `isBlank`, and default-value branches inside service methods.
- **Web DTO to Service Boundary**: Do not pass web request DTOs or web-only command wrapper records into
  `core.domain.service` methods. If the request already represents a domain object, create that domain object at the
  controller/request boundary. If it only supplies a few values, pass normalized values explicitly instead of adding a
  `core.domain.command` record just to carry web fields.
- **Fallback Visibility**: Broad fallback behavior in `core` must be a product rule, not a silent defensive catch-all. If a
  service falls back after repository, external, or runtime failure, the failure must remain observable through logging,
  result metadata, or a clearly documented caller contract.
- **Domain Behavior Extraction**: Move only object-local business meaning into domain objects:
  status transitions, authenticated owner/participant checks, and value merge/default semantics that belong to the
  object itself. Keep storage lookup, transaction boundaries, caller orchestration, and storage atomic guards in
  services/storage persistence types.
- **Application Service Boundary**: Do not remove service methods just because a domain object gained behavior.
  Preserve read facades and compatibility delegates until production and test callers are audited. Services should
  fetch required persisted state first, then call domain guard methods such as `require...By(...)`, then invoke
  storage persistence types.
- **Record to Domain Conversion**: In `core-api` services, convert storage Records to domain models directly with
  `new DomainModel(...)` at the service call path. Do not add service-local `toModel`/`toDomain` helpers, and do not
  move those conversions onto storage Records.
- **Domain Import Boundary**: Domain objects may use core/support business exceptions as existing domain precedent
  does, but must not import JPA/storage Records, web DTOs, controller contracts, or persistence primitives.

## Verification

- **Unit Testing**: All business logic in services and domain objects should be covered by JUnit 5 tests.
- **Domain Package Isolation**: Spring stereotype annotations are allowed by this module's component model, but domain
  pure domain model packages must not import web MVC, security-web, request DTO, JPA, JDBC, or storage Records.
  Application services may import storage Record/MyBatis types directly; do not reintroduce core-domain repository interfaces.
