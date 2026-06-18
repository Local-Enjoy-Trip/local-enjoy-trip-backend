<!-- AUTONOMY DIRECTIVE — DO NOT REMOVE -->
YOU ARE AN AUTONOMOUS CODING AGENT. EXECUTE TASKS TO COMPLETION WITHOUT ASKING FOR PERMISSION.
DO NOT STOP TO ASK "SHOULD I PROCEED?" — PROCEED. DO NOT WAIT FOR CONFIRMATION ON OBVIOUS NEXT STEPS.
IF BLOCKED, TRY AN ALTERNATIVE APPROACH. ONLY ASK WHEN TRULY AMBIGUOUS OR DESTRUCTIVE.
USE CODEX NATIVE SUBAGENTS FOR INDEPENDENT PARALLEL SUBTASKS WHEN THAT IMPROVES THROUGHPUT. THIS IS COMPLEMENTARY TO OMX TEAM MODE.
<!-- END AUTONOMY DIRECTIVE -->

## Stack

- Spring MVC
- MyBatis mapper/XML for persistence SQL
- Testcontainers for PostGIS/pg_vector integration verification
- Lombok (Required: Use `@RequiredArgsConstructor`, `@AllArgsConstructor`, or `@NoArgsConstructor` to eliminate constructor boilerplate)
- PostgreSQL/PostGIS
- Multi Module

## architecture

The project is organized around the monolithic `core-api` target shape plus lower-level storage and batch/runtime support:

- `core:core-api`: primary Spring Boot executable module.
  - API entrypoint: `com.ssafy.enjoytrip.EnjoyTripApplication`.
  - Worker entrypoint: `com.ssafy.enjoytrip.core.api.worker.EnjoyTripWorkerApplication`.
  - HTTP/API code lives under `com.ssafy.enjoytrip.core.api.web.*`.
  - Kafka/Scheduled/background worker ingress lives under `com.ssafy.enjoytrip.core.api.worker.*`.
  - Domain models, application services, outbound integration clients, and support contracts live here; database access uses storage entity/MyBatis types directly.
  - Concrete outbound integration clients used by the API are compiled in `core-api`; batch-only embedding clients are compiled in `batch`; the `external` module must not depend on `core-api`.
  - `:core:core-api:check` is the primary executable-module verification command.
- `core:core-enum`: enum-only shared module for values used by both `core-api` and `db-core`.
- `storage:db-core`: storage row/entity contracts, MyBatis mapper interfaces/XML/type handlers, Flyway migrations, and database configuration.
  - Do not place web/controller, worker ingress, domain service, or external API client code here.
- `external`: independent placeholder module with no `core-api` dependency. API-facing third-party clients live in `core-api`; batch-only embedding clients live in `batch`.
- `batch`: separate batch runtime. Batch job ingress, parameter parsing, batch-only services, and batch-only outbound clients stay in `batch`.

The legacy `app`, `app/web`, and `app/worker` modules are removed in the target shape. Do not add
them back.

Default request flow:

```text
core-api web controller -> core-api service -> storage entity/MyBatis
```

Default worker flow:

```text
core-api worker ingress -> core-api service/processor -> storage entity/MyBatis
```

Layering rules:

- Controllers should be thin. They translate HTTP requests into service calls and return HTTP responses.
- Controllers must use named request/response DTO objects. Do not use `Map`, `Map.of(...)`, `@RequestParam Map`, `@RequestBody Map`, or `ApiResponse<Map<...>>` for controller request/response contracts.
- Worker ingress should only translate runtime messages/events into service or processor calls.
- Web packages must not own Kafka listener, scheduled worker, or background-only retry/error handler code.
- Worker packages must not own controllers, OpenAPI contracts, REST Docs, web DTOs, or REST response envelopes.
- Controllers and workers must not call persistence/MyBatis mappers directly.
- Services may import storage entity/MyBatis types; web controllers and worker ingress must not call storage/MyBatis mappers directly.
- When services convert storage entities into core domain models, instantiate the domain model directly with `new` at the
  service call path. Do not hide entity-to-domain conversion behind service-local `toModel`/`toDomain` helpers, and do
  not put core-api domain model conversion methods on `storage:db-core` entities.
- Web request DTOs must not be passed into `core.domain.service` methods, and web-only command wrapper records should
  not live under `core.domain`. Controllers should pass domain models, existing core query/value objects, or explicitly
  unpacked normalized primitive/value parameters to services.
- `core:core-enum` must stay enum-only.

---

## Project Rule Hierarchy

Project server/runtime work must use the three-layer rule structure below.

1. `CONSTITUTION.md` — mandatory constitution
   - Read this first before changing project server/runtime code.
   - It is the highest-priority project rule document.
   - If any module `AGENTS.md`, local convention, plan, or precedent conflicts with it, follow `CONSTITUTION.md`.
2. `RULES.md` — operational templates and concrete field rules
   - Use this for module-specific checklists, security templates, migration templates, and completion reporting.
   - It may only make the constitution more concrete; it must not weaken it.
3. `PRECEDENTS.md` — accumulated cases
   - Record repeated decisions, exceptions, and field cases here first.
   - When the same decision pattern appears 3 times, promote it to `RULES.md` and mark the precedents as promoted.

Session-start enforcement:

- A Codex `SessionStart` hook is configured to load `CONSTITUTION.md` into session context for this repository.
- A Codex `Stop` hook is configured to check changed project module files against `CONSTITUTION.md` and `RULES.md` before a turn finishes.
- Even if the hook is unavailable, agents must manually read and follow `CONSTITUTION.md` before project server/runtime edits.

Completion reports for project server/runtime changes must state which layer was used: `CONSTITUTION.md`, `RULES.md`, and/or `PRECEDENTS.md`.
