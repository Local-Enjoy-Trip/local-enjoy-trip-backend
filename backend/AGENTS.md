<!-- AUTONOMY DIRECTIVE — DO NOT REMOVE -->
YOU ARE AN AUTONOMOUS CODING AGENT. EXECUTE TASKS TO COMPLETION WITHOUT ASKING FOR PERMISSION.
DO NOT STOP TO ASK "SHOULD I PROCEED?" — PROCEED. DO NOT WAIT FOR CONFIRMATION ON OBVIOUS NEXT STEPS.
IF BLOCKED, TRY AN ALTERNATIVE APPROACH. ONLY ASK WHEN TRULY AMBIGUOUS OR DESTRUCTIVE.
USE CODEX NATIVE SUBAGENTS FOR INDEPENDENT PARALLEL SUBTASKS WHEN THAT IMPROVES THROUGHPUT. THIS IS COMPLEMENTARY TO OMX TEAM MODE.
<!-- END AUTONOMY DIRECTIVE -->

## Stack

- Spring MVC
- Spring Data JPA
- jOOQ for complex SQL and native mutations
- Lombok (Required: Use `@RequiredArgsConstructor`, `@AllArgsConstructor`, or `@NoArgsConstructor` to eliminate constructor boilerplate)
- PostgreSQL/PostGIS
- Multi Module

## architecture

The backend is organized as a multi-module architecture with an application assembly namespace,
executable boundary modules, and shared lower-level modules:

- `app`: source-free Gradle assembly namespace for executable applications.
  - Owns no business, web, worker, persistence, or integration source code.
  - Do not add `backend/app/src/main` or `backend/app/src/test` source sets.
  - Runnable boundaries live under `backend/app/web` and `backend/app/worker`.
  - `backend:app:check` delegates to `backend:app:web:check` and `backend:app:worker:check`.
- `app:web`: HTTP/API executable.
  - Owns Spring MVC controllers, request/response DTOs, validation, exception handling, OpenAPI/REST Docs, security configuration, and HTTP JSON contracts.
  - Depends on `core`, `external`, support modules, and may depend on `storage` only for Spring Boot assembly wiring.
  - Must not import any `com.ssafy.enjoytrip.storage.*` type and must not place persistence adapters under `app:web`.
  - Must not own Kafka CDC consumers, scheduled workers, or background-only retry/error handler infrastructure.
  - `backend:app:web:check` enforces source-level isolation from storage implementation packages and worker-only listener code.
- `app:worker`: async/background executable.
  - Owns Kafka consumers, retry/error handling, scheduled/background workers, and future outbox processing infrastructure.
  - Calls `core` services/processors and may depend on `storage` only for Spring Boot assembly wiring.
  - Must not import any `com.ssafy.enjoytrip.storage.*` type and must not place persistence adapters under `app:worker`.
  - Must not own controllers, OpenAPI contracts, REST Docs, web DTOs, or REST response envelopes.
  - `backend:app:worker:check` enforces source-level isolation from storage implementation packages and web/controller code.
- `core`: domain and application ports.
  - Owns domain models and repository interfaces.
  - Must not depend on `app`, `app:web`, `app:worker`, `storage`, or `external`.
- `storage`: persistence layer.
  - Owns JPA entities, Spring Data repositories, persistence DTOs, database-specific configuration, and JPA-backed repository adapter implementations.
  - Depends on `core` and implements repository interfaces from `core`.
  - Use JPA repositories for normal CRUD and simple queries.
  - Use jOOQ for complex read queries that need dynamic predicates, joins, projections, or composable filtering.
  - If a native query is unavoidable for update/delete operations, implement it through jOOQ, not JPA `@Query(nativeQuery = true)` or Spring JDBC template APIs.
  - Handles PostgreSQL/PostGIS access only; do not place web/controller or worker ingress logic here.
- `external`: outbound integration layer.
  - Owns clients, adapters, configuration, and DTOs for third-party APIs or external systems.
  - Keeps external API details isolated from controllers and core application services.

Default request flow:

```text
controller -> service -> repository interface -> storage adapter
```

Default worker flow:

```text
worker ingress -> service/processor -> repository interface -> storage adapter
```

Layering rules:

- Controllers should be thin. They translate HTTP requests into service calls and return HTTP responses.
- Controllers must use named request/response DTO objects. Do not use `Map`, `Map.of(...)`, `@RequestParam Map`, `@RequestBody Map`, or `ApiResponse<Map<...>>` for controller request/response contracts.
- Services contain application/business logic and transaction boundaries.
- Domain models live in `core/domain`; repository interfaces live in `core/repository`.
- JPA-backed adapters live only in `storage/repository` and use the `storage` module's entities and Spring Data repositories.
- No source under `backend/app/web/src/main` or `backend/app/worker/src/main` may import or reference `com.ssafy.enjoytrip.storage.*`.
- Controllers and workers must not call repositories directly.
- `app:web`, `app:worker`, `storage`, and `external` must not depend on each other through source-level imports except for allowed `app:web -> external` client usage and executable modules' Spring Boot assembly dependency on `storage`. Shared application contracts go through `core`.

---

## Backend Rule Hierarchy

Backend work must use the three-layer rule structure below.

1. `backend/CONSTITUTION.md` — mandatory constitution
   - Read this first before changing backend code.
   - It is the highest-priority backend rule document.
   - If any backend `AGENTS.md`, local convention, plan, or precedent conflicts with it, follow `CONSTITUTION.md`.
2. `backend/RULES.md` — operational templates and concrete field rules
   - Use this for module-specific checklists, security templates, migration templates, and completion reporting.
   - It may only make the constitution more concrete; it must not weaken it.
3. `backend/PRECEDENTS.md` — accumulated cases
   - Record repeated decisions, exceptions, and field cases here first.
   - When the same decision pattern appears 3 times, promote it to `RULES.md` and mark the precedents as promoted.

Session-start enforcement:

- A Codex `SessionStart` hook is configured to load `backend/CONSTITUTION.md` and `backend/RULES.md` into session context for this repository.
- Even if the hook is unavailable, agents must manually read and follow `backend/CONSTITUTION.md` before backend edits.

Completion reports for backend changes must state which layer was used: `CONSTITUTION.md`, `RULES.md`, and/or `PRECEDENTS.md`.
