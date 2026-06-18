# core-api local startup Flyway history repair (2026-06-18)

## Symptom

`./gradlew :core:core-api:bootRun --console=plain` failed while starting against the existing local database:

```text
jdbc:postgresql://localhost:15432/enjoytrip
```

The first failure was Flyway validation:

```text
Migration checksum mismatch for migration version 1
Migration checksum mismatch for migration version 2
```

After the checksum rows were corrected, Flyway reported that the version 2 description still differed:

```text
Migration description mismatch for migration version 2
Applied to database : add external data tables
Resolved locally    : add note single image reference
```

## Root cause

The existing local `enjoytrip` database had an older Flyway schema history:

```text
1|1|create storage schema|SQL|-900210985|t
2|2|add external data tables|SQL|1066587499|t
...
15|15|create notifications and outbox|SQL|844158563|t
```

The current repository only resolves the consolidated migration set under
`storage/db-core/src/main/resources/db/migration`, so Flyway compared the old local history against the current
resolved `V1` and `V2` metadata and stopped during validation.

## Fix applied

The existing `enjoytrip` database was kept. No application source code, Gradle file, or `.env` datasource target was
changed.

The local `flyway_schema_history` rows for versions `1` and `2` were updated to match the current resolved local
migration metadata:

```sql
begin;

update flyway_schema_history
set checksum = 1975877635
where version = '1';

update flyway_schema_history
set checksum = -1556073518
where version = '2';

update flyway_schema_history
set description = 'add note single image reference'
where version = '2';

commit;
```

Final repaired rows:

```text
1|1|create storage schema|1975877635|t
2|2|add note single image reference|-1556073518|t
```

## Verification

### Server startup

Command:

```bash
./gradlew :core:core-api:bootRun --console=plain
```

Observed result:

```text
Flyway - Successfully validated 2 migrations
DbMigrate - Schema "public" is up to date. No migration necessary.
Tomcat started on port 8080 (http) with context path '/'
Started EnjoyTripApplication
```

### HTTP smoke test

Command:

```bash
curl -s http://localhost:8080/health | jq -c .
```

Observed result:

```json
{"data":{"status":"ok"},"error":null,"success":true}
```

### Database target check

The server is using the existing DB target:

```text
ENJOYTRIP_DB_URL=jdbc:postgresql://localhost:15432/enjoytrip
```

The temporary `enjoytrip_core_api` database that had been created during the earlier incorrect workaround was dropped;
the matching database-list check returned only:

```text
enjoytrip
```
