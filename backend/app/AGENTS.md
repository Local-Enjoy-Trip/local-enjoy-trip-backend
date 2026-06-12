# App Assembly Module Rules

## Role

- `backend/app` is a source-free Gradle assembly namespace for executable backend applications.
- It exists to group runnable boundaries under `backend/app/web` and `backend/app/worker`.
- It may define aggregate Gradle tasks that delegate to child executable modules.

## Forbidden

- Do not add `src/main` or `src/test` under `backend/app`.
- Do not place controllers, DTOs, workers, services, adapters, configuration, or business logic directly in this module.
- Do not use this module as a shared library. Shared application contracts belong in `backend/core`; executable ingress belongs in child modules.

## Verification

- Run `./gradlew :backend:app:check` after changing this module.
- If child executable boundaries are affected, also run the targeted child checks:
  - `./gradlew :backend:app:web:check`
  - `./gradlew :backend:app:worker:check`
