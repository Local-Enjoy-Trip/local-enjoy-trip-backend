# Dongnepin migration cleanup

## Scope

This branch is the Phase A migration cleanup for `.omx/plans/dongnepin-epic2-backend-plan.md`.
It consolidates the previous `V1..V15` Flyway migrations into a single fresh-database baseline:

- `storage/src/main/resources/db/migration/V1__create_storage_schema.sql`

No Epic 2 API, MinIO, map exploration, or note image feature code is implemented in this phase.

## Compatibility boundary

This cleanup is a **disposable/local/dev baseline reset**. It changes already-versioned Flyway migration
content and removes previous version files, so an existing database that already has the previous
`flyway_schema_history` entries should expect validation/checksum mismatch.

Do not run `flyway repair`, truncate `flyway_schema_history`, or reset a shared/production database for this
branch without explicit user approval and a backup/restore plan.

If a database with existing Flyway history must remain deployable without reset/repair, this cleanup branch is
not the right delivery shape; keep the old migrations and add a new corrective migration instead.

## Verification targets

Phase A is complete only after recording fresh evidence for:

1. `./gradlew :storage:db-core:generateJooq`
2. `./gradlew :storage:db-core:test`
3. `./gradlew :core:core-api:check`
4. `./gradlew :core:core-api:check`
5. Fresh database Flyway migration and direct schema inspection when local Docker/Postgres is available.


## Verification evidence (2026-06-15)

Executed in fresh worktree `feature/dongnepin-migration-cleanup` with a disposable Docker PostgreSQL/PostGIS/pgvector container on `localhost:15433`.

- `./gradlew :storage:db-core:generateJooq` → `BUILD SUCCESSFUL`.
- `./gradlew :storage:db-core:test :core:core-api:check :core:core-api:check` → `BUILD SUCCESSFUL`.
- Runtime/Flyway proof used `./gradlew :core:core-api:bootRun` with:
  - `ENJOYTRIP_DB_URL=jdbc:postgresql://localhost:15433/enjoytrip`
  - dummy Google OAuth client values for local boot only.
- `GET /health` returned:

```json
{"data":{"status":"ok"},"error":null,"success":true}
```

- `flyway_schema_history` on the fresh DB contained exactly the consolidated baseline:

```text
1|1|create storage schema|t
```

- Direct schema inspection on the fresh DB confirmed representative tables/constraints:

```text
attraction_embeddings|17
friendships|8
members|12
notes|14
notifications|9
chk_friendships_status|c
chk_notes_visibility|c
uk_notifications_business_reference|i
uk_members_email|u
```

## Pre-cleanup-history DB policy

For this Ralph run, the pre-cleanup-history path is documented rather than repaired: an old-history DB is expected
to fail validation after this squash because previous migration versions were intentionally consolidated. Use a
fresh local disposable database for runtime proof.

## 2026-06-20 Debezium/Kafka/ClickHouse removal note

The notification delivery path no longer uses a local outbox table. New databases still run the historical
consolidated baseline and then the follow-up removal migration, which drops the old outbox table and keeps
`notifications` protected by a business-reference unique index.
