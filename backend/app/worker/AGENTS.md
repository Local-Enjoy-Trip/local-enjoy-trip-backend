# Worker Module Coding Style & Rules

## Operating Principles

- **Entry Point**: The `worker` module is the asynchronous/background executable. It owns Kafka consumers,
  retry/error handling, scheduled/background processing, and future outbox workers.
- **No HTTP Ownership**: Do not add controllers, OpenAPI contracts, REST Docs, web DTOs, `ApiResponse`
  response contracts, or `spring-boot-starter-web` unless a concrete worker HTTP requirement is explicitly
  approved and documented.
- **Core First**: Worker code calls `core` services/processors. Business behavior and transaction policy
  stay in `core`; worker code only translates runtime messages/events into core calls.
- **Strict Source Isolation**: Must not import any `com.ssafy.enjoytrip.storage.*` types in `worker` source.
  Persistence details are accessed through `core` ports/services. The module may depend on `backend:storage`
  only for Spring Boot assembly wiring.
- **Observable Failures**: Background processing failures must be observable through logs and durable state
  such as outbox status/attempt/error updates. Do not silently swallow worker failures.

## Coding Style

- Keep worker message parsing near the worker ingress boundary.
- Normalize external runtime payloads before calling `core` processors.
- Use constructor injection with `@RequiredArgsConstructor` for collaborators.
- Keep retry/backoff policy explicit and covered by tests when failure behavior changes.
- Keep worker application configuration in `backend/app/worker/src/main/resources/application.yml`.

## Verification

- Run `./gradlew :backend:app:worker:check` after worker changes.
- `backend:app:worker:check` must fail if worker source imports storage implementation packages or owns web/controller/API/REST response code.
- Worker runtime should use `spring.main.web-application-type: none` and should not include `spring-boot-starter-web` by default.
