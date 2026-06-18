# External Module Rules

## Role

- `external` is kept as an independent module boundary and must not depend on `core-api`.
- Current API-facing outbound integration implementations live in `core-api` under `com.ssafy.enjoytrip.external.*`; batch-only outbound implementations live in `batch`.
- Do not add source code here that imports `com.ssafy.enjoytrip.core.*`.

## Verification

- Run `./gradlew :external:check` after modifying this module.
- If API-facing outbound client behavior changes, verify it through `:core:core-api:check`; if batch-only outbound behavior changes, verify it through `:batch:check`.
