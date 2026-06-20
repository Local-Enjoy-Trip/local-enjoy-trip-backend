# Core API Module Rules

## Role

- `core/core-api` is the monolithic Spring Boot executable module for the migration target.
- It owns both entrypoints:
  - API: `com.ssafy.enjoytrip.EnjoyTripApplication`
  - Worker: `com.ssafy.enjoytrip.core.api.worker.EnjoyTripWorkerApplication`
- It also owns domain/application logic and support contracts. API-facing outbound clients and neutral result DTOs live in `external`; core-api services inject those concrete clients directly and map result DTOs to domain objects at the service call path. Batch-only embedding clients live in `batch`. The `external` module must not depend on `core-api` or `core`. Database access uses storage Record/MyBatis types directly.

## Package Boundaries

- HTTP/API code belongs under `com.ssafy.enjoytrip.core.api.web.*`.
- API configuration/security/filter code belongs under `com.ssafy.enjoytrip.core.api.config.*`,
  `com.ssafy.enjoytrip.core.api.security.*`, and `com.ssafy.enjoytrip.core.api.filter.*`.
- Worker ingress belongs under `com.ssafy.enjoytrip.core.api.worker.*`.
- Domain/application logic belongs under `com.ssafy.enjoytrip.core.domain.*`.
- Do not create repository packages under `com.ssafy.enjoytrip.core.domain`; use `storage:db-core` Record/MyBatis types from core-api when persistence is needed.
- Shared support contracts belong under `com.ssafy.enjoytrip.core.support.*`.
- API-facing concrete outbound integration clients belong in the `external` module under `com.ssafy.enjoytrip.external.*`; batch-only clients belong under `batch`. Do not reintroduce `core.domain.external.*` or core-owned outbound interface/port/gateway packages just to preserve the old external-module split.

## Forbidden

- Do not place scheduled workers or background-only retry/error handler infrastructure under
  `com.ssafy.enjoytrip.core.api.web.*`.
- Do not place controllers, OpenAPI contracts, REST Docs, web DTOs, or REST response envelopes under
  `com.ssafy.enjoytrip.core.api.worker.*`.
- Controllers and worker ingress must not call MyBatis mappers or storage persistence types directly; go through services.
- Storage Record/MyBatis imports are allowed in core-api service/storage helper code, not in web controllers or worker ingress.
- When a core-api service converts a storage Record into a domain model, instantiate the domain model directly at the
  service call path with `new` for one-off conversions. If the same long storage Record -> domain model conversion
  repeats inside one service, a private service-local helper is allowed for duplicate removal.
- Do not move Record-to-domain conversion methods onto `storage:db-core` Records; that would make persistence types
  depend on the core-api domain model and blur the storage boundary.
- Do not pass web request DTOs or web-only command wrapper records into core-api services. Controllers should pass
  domain models, existing core query/value objects, or explicitly unpacked normalized primitive/value parameters.
- Do not reintroduce `app`, `app:web`, or `app:worker`.

## Worker Runtime

- Worker-specific properties live in `src/main/resources/application-worker.yml`.
- `EnjoyTripWorkerApplication` activates the `worker` profile and must keep `spring.main.web-application-type: none`.
- Scheduled/background worker behavior must remain observable through logs and durable storage state when retry or reconciliation is required.

## Verification

- Run `./gradlew :core:core-api:check` after modifying this module.
- If worker ingress changes, ensure equivalent worker tests cover scheduler/retry/failure behavior.
- If public API changes, also prove the JSON response shape with a real HTTP request when local runtime dependencies are available.
