# App Module Coding Style & Rules

## Operating Principles

- **Entry Point**: The `app` module serves as the application entry point and the web/API layer. It orchestrates business logic by calling `core` services.
- **Thin Controllers**: Controllers should be thin. Their primary responsibility is translating incoming HTTP requests into service calls and returning structured HTTP responses.
- **Strict Isolation**: Must not import any `com.ssafy.enjoytrip.storage.*` types. Persistence details are fully encapsulated behind `core` repository interfaces.

## Coding Style

- **Package Structure & Reorganization**:
  - **DTOs**: Reorganized into distinct packages under `dto` based on their role:
    - **`com.ssafy.enjoytrip.web.dto.request`**: All request DTOs (e.g., `MemberRequest`, `AttractionSearchRequest`).
    - **`com.ssafy.enjoytrip.web.dto.response`**: All response DTOs (e.g., `LoginResponse`, `AttractionsResponse`).
  - **API Contracts**: Documentation interfaces (using Swagger/OpenAPI annotations like `@Operation`, `@ApiResponses`) must be placed in **`com.ssafy.enjoytrip.web.api`** (e.g., `AttractionApi.java`).
  - **Controllers**: Controller implementations must be placed in **`com.ssafy.enjoytrip.web.controller`** (e.g., `AttractionController.java`).
- **Explicit Contract Dependencies**: By separating `api` and `controller` packages, the controller implementation must explicitly import its corresponding API interface (e.g., `import com.ssafy.enjoytrip.web.api.AttractionApi;`). This clearly shows the contract dependency in the import block.
- **Request/Response DTOs**: Controllers must use dedicated request/response DTOs (e.g., `MemberRequest`, `AttractionsResponse`). Do not use `Map`, `Map.of(...)`, or `@RequestParam Map` for defining controller contracts.
- **Error Handling**: Use custom exceptions like `CoreException` coupled with specific `ErrorType` values. A `GlobalExceptionHandler` processes these into standardized HTTP error responses.
- **Dependency Injection**: Use `@RequiredArgsConstructor` for constructor injection of `final` service dependencies. Avoid manual constructors.

## Verification

- **API Response Consistency**: Always wrap controller responses in `ApiResponse<T>`, utilizing `success()` or `fail()` helper methods to maintain a consistent JSON structure across all endpoints.
