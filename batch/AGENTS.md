# Batch Module Coding Style & Rules

- `batch` owns manual/offline Spring Batch jobs only.
- Do not add controllers, schedulers, cron triggers, or public semantic APIs here.
- Batch orchestration owns batch-only services locally and uses `storage:db-core` for DB access. Do not depend on `core-api` for batch-only work.
- Outbound client implementations used only by batch are owned by `batch`; secrets/config are injected through `enjoytrip.*` properties.
- Live jobs must fail fast when required target-region proof or GMS API key configuration is missing.
- Job parameters, property strings, target-region configuration, and other batch ingress values must be parsed, validated,
  and normalized in `batch` configuration/launcher code before calling batch services. Do not push raw job arguments into shared services
  services for presence checks, string parsing, or default-value repair.
