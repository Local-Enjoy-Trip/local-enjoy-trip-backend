# Core Module Coding Style & Rules

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
- **Fallback Visibility**: Broad fallback behavior in `core` must be a product rule, not a silent defensive catch-all. If a
  service falls back after repository, external, or runtime failure, the failure must remain observable through logging,
  result metadata, or a clearly documented caller contract.

## Verification

- **Unit Testing**: All business logic in services and domain objects should be covered by JUnit 5 tests.
- **No Web/Persistence Infrastructure in Core**: Spring stereotype annotations are allowed by this module's component model,
  but web MVC, security-web, request DTO, JPA, JDBC, and persistence implementation details belong to `app` or `storage`.
  Do not import `jakarta.persistence.*`, controller/web types, storage entities, or persistence adapters in `core`.
