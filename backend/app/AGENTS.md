# App Module Coding Style & Rules

## Operating Principles

- **Entry Point**: The `app` module serves as the application entry point and the web/API layer. It orchestrates business logic by calling `core` services.
- **Thin Controllers**: Controllers should be thin. Their primary responsibility is translating incoming HTTP requests into service calls and returning structured HTTP responses.
- **Strict Source Isolation**: Must not import any `com.ssafy.enjoytrip.storage.*` types in `app` source. Persistence details are accessed through `core` repository interfaces. Because `app` is the Spring Boot assembly module, its Gradle build may depend on `backend:storage` for application wiring, but controllers/configuration must not directly use storage implementation types.

## Coding Style

- **Package Structure & Reorganization**:
  - **DTOs**: Reorganized into distinct packages under `dto` based on their role:
    - **`com.ssafy.enjoytrip.web.dto.request`**: All request DTOs (e.g., `MemberRequest`, `AttractionSearchRequest`).
    - **`com.ssafy.enjoytrip.web.dto.response`**: All response DTOs (e.g., `LoginResponse`, `AttractionsResponse`).
  - **API Contracts**: Documentation interfaces (using Swagger/OpenAPI annotations like `@Operation`, `@ApiResponses`) must be placed in **`com.ssafy.enjoytrip.web.api`** (e.g., `AttractionApi.java`).
  - **Controllers**: Controller implementations must be placed in **`com.ssafy.enjoytrip.web.controller`** (e.g., `AttractionController.java`).
- **Explicit Contract Dependencies**: By separating `api` and `controller` packages, the controller implementation must explicitly import its corresponding API interface (e.g., `import com.ssafy.enjoytrip.web.api.AttractionApi;`). This clearly shows the contract dependency in the import block.
- **Request/Response DTOs**: Controllers must use dedicated request/response DTOs (e.g., `MemberRequest`, `AttractionsResponse`). Do not use `Map`, `Map.of(...)`, or `@RequestParam Map` for defining controller contracts.
- **REST JSON Contracts**: Mutating endpoints (`POST`, `PUT`, `PATCH`) must receive typed JSON bodies with `@RequestBody @Valid`. Do not keep legacy action-dispatch endpoints or form-style mutation contracts for compatibility unless the user explicitly requests backward compatibility. `GET` endpoints may bind query parameters through named DTOs, including Spring MVC query-object binding such as `@ModelAttribute`, when that keeps query contracts typed and readable.
- **Spring Validation First**: Do not add repetitive null/blank/range validation branches in controllers when the rule can be expressed on request DTOs. Put Bean Validation annotations such as `@NotBlank`, `@NotNull`, `@Positive`, `@Min`, `@Size`, or `@Pattern` on request DTO fields, apply `@Valid`/`@Validated` at controller boundaries, and let `GlobalExceptionHandler` convert validation failures into standardized API errors. Keep only validations that require authenticated principal, ownership, persisted state, or cross-resource business checks in services/application flow.
- **Error Responses**: Controllers must not define private `fail(...)` helpers or manually build ad-hoc error bodies. Use `ApiResponse.fail(ErrorType)` when directly constructing an error `ResponseEntity`, or throw `CoreException` so `GlobalExceptionHandler` can set the HTTP status and standardized error envelope.
- **Ingress Parsing Boundary**: Structured query strings, legacy form blobs, and raw request fields must be parsed and
  normalized in `app` request DTOs, mappers, or controller-adjacent helpers before calling `core`. Do not push raw HTTP
  strings into `core` services for parsing, trimming, coercion, or default-value repair.
- **Error Handling**: Use custom exceptions like `CoreException` coupled with specific `ErrorType` values. A `GlobalExceptionHandler` processes these into standardized HTTP error responses.
- **Dependency Injection**: Use `@RequiredArgsConstructor` for constructor injection of `final` service dependencies. Avoid manual constructors.

## Verification

- **API Response Consistency**: Always wrap controller responses in `ApiResponse<T>`, utilizing `success()` or `fail()` helper methods to maintain a consistent JSON structure across all endpoints.
