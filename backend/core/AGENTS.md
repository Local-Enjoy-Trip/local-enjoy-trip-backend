# Core Module Coding Style & Rules

## Operating Principles

- **Spring Boot Native**: The `core` module is built to be a Spring Boot native module. It uses `@Service` and `@Component` annotations to support Auto-Configuration and Component Scanning in the consuming modules.
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

## Verification

- **Unit Testing**: All business logic in services and domain objects should be covered by JUnit 5 tests.
- **No Infrastructure in Core**: Avoid importing `org.springframework.*` or `jakarta.persistence.*` in the `core` module. Infrastructure concerns belong to the `storage` or `app` modules.
