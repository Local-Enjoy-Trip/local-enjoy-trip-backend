# Batch Module Coding Style & Rules

- `backend:batch` owns manual/offline Spring Batch jobs only.
- Do not add controllers, schedulers, cron triggers, or public semantic APIs here.
- Batch orchestration must call `core` ports/services and keep DB writes behind `storage` adapters.
- External API contracts must remain in `external`; secrets/config are injected through `enjoytrip.*` properties.
- Live jobs must fail fast when required target-region proof or GMS API key configuration is missing.
- Job parameters, property strings, target-region configuration, and other batch ingress values must be parsed, validated,
  and normalized in `batch` configuration/launcher code before calling `core`. Do not push raw job arguments into `core`
  services for presence checks, string parsing, or default-value repair.
