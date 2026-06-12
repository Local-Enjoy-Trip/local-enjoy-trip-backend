# Web Module Coding Style & Rules

## Operating Principles

- **Entry Point**: The `web` module is the HTTP/API executable. It owns Spring MVC controllers,
  request/response DTOs, validation, exception handling, OpenAPI/REST Docs, security configuration,
  and HTTP JSON contracts.
- **Thin Controllers**: Controllers translate HTTP requests into service calls and return structured
  HTTP responses. Business behavior belongs in `core` services.
- **Strict Source Isolation**: Must not import any `com.ssafy.enjoytrip.storage.*` types in `web` source.
  Persistence details are accessed through `core` repository interfaces. Because `web` is a Spring Boot
  assembly module, its Gradle build may depend on `backend:storage` for application wiring, but
  controllers/configuration must not directly use storage implementation types.
- **No Worker Ownership**: Kafka CDC consumers, scheduled workers, retry/error handler infrastructure,
  and background-only processing belong in `backend:worker`, not `backend:web`.

## Coding Style

- **Package Structure & Reorganization**:
  - **DTOs**: Keep request DTOs under `com.ssafy.enjoytrip.web.dto.request` and response DTOs under `com.ssafy.enjoytrip.web.dto.response`.
  - **API Contracts**: Documentation interfaces using Swagger/OpenAPI annotations belong in `com.ssafy.enjoytrip.web.api`.
  - **Controllers**: Controller implementations belong in `com.ssafy.enjoytrip.web.controller` and explicitly import their API interface.
- **Request/Response DTOs**: Controllers must use dedicated request/response DTOs. Do not use `Map`,
  `Map.of(...)`, `@RequestParam Map`, `@RequestBody Map`, or `ApiResponse<Map<...>>` for controller contracts.
- **REST JSON Contracts**: Mutating endpoints (`POST`, `PUT`, `PATCH`) must receive typed JSON bodies
  with `@RequestBody @Valid`. Do not keep legacy action-dispatch endpoints or form-style mutation
  contracts for compatibility unless the user explicitly requests backward compatibility. `GET` endpoints
  may bind query parameters through named DTOs, including Spring MVC query-object binding such as
  `@ModelAttribute`, when that keeps query contracts typed and readable.
- **Resource-Oriented URI Naming**: URI paths should represent resources (nouns) rather than actions (verbs).
  Use standard HTTP methods for CRUD. Complex workflow actions may use action subresources with `POST`
  when CRUD modeling is unnatural.
- **Spring Validation First**: Put Bean Validation annotations on request DTO fields and use
  `@Valid`/`@Validated` at controller boundaries. Keep only validations requiring authenticated principal,
  ownership, persisted state, or cross-resource business checks in services/application flow.
- **Error Responses**: Controllers must not define private `fail(...)` helpers or manually build ad-hoc
  error bodies. Use `ApiResponse.fail(ErrorType)` when directly constructing an error `ResponseEntity`,
  or throw `CoreException` so `GlobalExceptionHandler` can set the HTTP status and standardized error envelope.
- **Ingress Parsing Boundary**: Structured query strings, legacy form blobs, and raw request fields must be
  parsed and normalized in `web` request DTOs, mappers, or controller-adjacent helpers before calling `core`.
- **Dependency Injection**: Use `@RequiredArgsConstructor` for constructor injection of `final` service
  dependencies. Avoid manual constructors.

## Verification

- Run `./gradlew :backend:web:check` after web changes.
- Public API changes should also be proven with a real HTTP JSON request against `:backend:web:bootRun`
  when local runtime dependencies are available.
- `backend:web:check` must fail if web source imports storage implementation packages or owns worker-only Kafka listener/outbox CDC code.
