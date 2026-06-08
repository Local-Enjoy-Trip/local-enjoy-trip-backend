# External Module Coding Style & Rules

## Operating Principles

- **Outbound Integration**: The `external` module is responsible for outbound communication with third-party APIs or external systems (e.g., Tour API, EV Charger API).
- **Encapsulation**: External API details, raw HTTP interactions, and data parsing logic must be fully isolated from controllers (`app`) and core application logic (`core`).

## Coding Style

- **Clients**: Implement explicit client classes annotated with `@Component` (e.g., `TourApiClient`). Use native Java `HttpClient` or other modern HTTP clients to perform requests.
- **Data Mapping**: Parse external XML or JSON responses internally within the client, and map the results strictly to `core/domain` models (e.g., mapping XML item nodes to the `Attraction` domain object).
- **Configuration & Secrets**: Do not read API keys or external configuration directly via `System.getenv()` or `System.getProperty()` in Java code. Define an `enjoytrip.*` property in `application.yml` that resolves environment/system values, then inject that property with `@Value`, `@ConfigurationProperties`, or Spring `Environment`. Do not hardcode API secrets in the source files.

## Verification

- **Error Handling**: Validate HTTP status codes explicitly. Throw standard exceptions like `IOException` or custom `ExternalServiceException` when external systems fail, timeout, or return unexpected results.
