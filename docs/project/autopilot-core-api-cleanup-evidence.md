# Autopilot Evidence: core-api/core-enum cleanup

## Plan artifact
- `docs/project/autopilot-core-api-cleanup-plan.md`

## Implementation summary
- Removed active Gradle includes for legacy root `:core`, `:storage`, and stale `:support:auth`; `:external` is active again as the outbound integration module.
- Kept active target graph: `:core:core-api`, `:core:core-enum`, `:storage:db-core`, `:external`, `:batch`, `:support:logging`, `:support:monitoring`.
- Removed source-bearing legacy `core/src` and `storage/src` remnants.
- Removed core-domain repository packages and removed custom storage repository/model wrappers from `storage:db-core`.
- Moved external provider/gateway/generator implementations to the active `external` module; `core.domain.external.*` keeps only service-facing contracts.
- Updated AGENTS/RULES/CONSTITUTION/README wording to the current storage entity/JPA/jOOQ direct module shape.
- Updated batch imports/dependencies to `core-api` target packages.

## Verification
- `./gradlew :core:core-api:check :storage:db-core:check --console=plain` ✅
- `./gradlew check --console=plain` ✅
- `git diff --check` ✅
- legacy search for old core-domain repository imports/custom storage repository wrappers and enum leftovers in `core-api` returned 0 rows ✅
- active project graph confirmed only target modules plus batch/support common/error ✅

## Residual risk
- Runtime boot/HTTP smoke proof was not run in this loop; this change is compile/test verified.
