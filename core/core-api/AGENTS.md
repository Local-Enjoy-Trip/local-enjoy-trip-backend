# Core API Module Rules

## Role

- `core/core-api` is the monolithic Spring Boot executable module for the migration target.
- It owns both entrypoints:
  - API: `com.ssafy.enjoytrip.EnjoyTripApplication`
  - Worker: `com.ssafy.enjoytrip.core.api.worker.EnjoyTripWorkerApplication`
- It also owns domain/application logic, external client contracts, and support contracts. Concrete outbound integration clients live in the active `external` module. Database access uses storage entity/JPA/jOOQ types directly.

## Package Boundaries

- HTTP/API code belongs under `com.ssafy.enjoytrip.core.api.web.*`.
- API configuration/security/filter code belongs under `com.ssafy.enjoytrip.core.api.config.*`,
  `com.ssafy.enjoytrip.core.api.security.*`, and `com.ssafy.enjoytrip.core.api.filter.*`.
- Worker ingress belongs under `com.ssafy.enjoytrip.core.api.worker.*`.
- Domain/application logic belongs under `com.ssafy.enjoytrip.core.domain.*`.
- Do not create repository packages under `com.ssafy.enjoytrip.core.domain`; use `storage:db-core` entity/JPA/jOOQ types from core-api when persistence is needed.
- Shared support contracts belong under `com.ssafy.enjoytrip.core.support.*`.
- External integration contracts belong under `com.ssafy.enjoytrip.core.domain.external.*`; concrete external implementations must not live in `core-api`.

## Forbidden

- Do not place Kafka listeners, scheduled workers, or background-only retry/error handler infrastructure under
  `com.ssafy.enjoytrip.core.api.web.*`.
- Do not place controllers, OpenAPI contracts, REST Docs, web DTOs, or REST response envelopes under
  `com.ssafy.enjoytrip.core.api.worker.*`.
- Controllers and worker ingress must not call JPA repositories or storage persistence types directly; go through services.
- Storage entity/JPA/jOOQ imports are allowed in core-api service/storage helper code, not in web controllers or worker ingress.
- When a core-api service converts a storage entity into a domain model, instantiate the domain model directly at the
  service call path with `new`. Do not hide that conversion behind service-local `toModel`/`toDomain` helpers.
- Do not move entity-to-domain conversion methods onto `storage:db-core` entities; that would make persistence types
  depend on the core-api domain model and blur the storage boundary.
- Do not pass web request DTOs or web-only command wrapper records into core-api services. Controllers should pass
  domain models, existing core query/value objects, or explicitly unpacked normalized primitive/value parameters.
- Do not reintroduce `app`, `app:web`, or `app:worker`.

## Worker Runtime

- Worker-specific properties live in `src/main/resources/application-worker.yml`.
- `EnjoyTripWorkerApplication` activates the `worker` profile and must keep `spring.main.web-application-type: none`.
- Kafka CDC behavior must remain observable through logs and durable outbox status/attempt/error updates.

## Verification

- Run `./gradlew :core:core-api:check` after modifying this module.
- If worker ingress changes, ensure `NotificationOutboxCdcConsumerTest` or equivalent worker tests cover ack/retry/failure behavior.
- If public API changes, also prove the JSON response shape with a real HTTP request when local runtime dependencies are available.
