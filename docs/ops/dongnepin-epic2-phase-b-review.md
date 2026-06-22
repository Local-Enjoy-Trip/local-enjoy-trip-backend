# Dongnepin Epic 2 Phase B Review Notes

> Worker-3 scope: review and documentation for `.omx/plans/dongnepin-epic2-backend-plan.md` Phase B.
> Timestamp: 2026-06-15.

## Reviewed source-of-truth

- `CONSTITUTION.md` from session-start rules, with `RULES.md` covered by the Stop governance check.
- Applicable module rules:
  - `AGENTS.md`
  - `app/web/AGENTS.md`
  - `core/AGENTS.md`
  - `storage/AGENTS.md`
  - `external/AGENTS.md`
- Phase B section of `.omx/plans/dongnepin-epic2-backend-plan.md`.

## Boundary checklist for implementation review

- `app/web` may own controllers, request/response DTOs, OpenAPI contracts, validation, and response envelopes.
- `app/web/src/main` must not import `com.ssafy.enjoytrip.storage.*`.
- `core` now requires explicit map coordinates; the old member-location fallback decision is obsolete.
- `storage` owns jOOQ/PostGIS queries, visibility/friend relationship projection, note image persistence, and migration.
- `external` owns MinIO/S3-compatible presigned upload client and config binding.
- `support/auth` owns security matcher protection for the new authenticated routes.

## Current baseline gaps before peer implementation integration

At the time this review document was written, this worker worktree still had the pre-Phase-B implementation shape:

- `GET /api/map/explore` was not present in tracked web controllers/API docs.
- `POST /api/note-images/presigned-upload` was not present in tracked web controllers/API docs.
- `Note`, `CreateNoteCommand`, and `UpdateNoteCommand` did not yet carry a single image reference.
- `NoteStorageRepository.findNearbyAccessible` already had a visibility predicate for legacy nearby notes, but did not yet project map-specific author nickname/profile masking or `SELF | FRIEND | NONE`.
- `SecurityConfig` protected note mutations, friendship, notifications, and plan mutations, but did not yet list `/api/map/explore` or `/api/note-images/presigned-upload`.
- MinIO properties, client, and local Compose service were not yet visible in this worktree.

These are review checkpoints rather than final defects; implementation workers may add them in separate worktrees before integration.

## Code review focus after implementation lands

1. **Security**
   - Verify both new endpoints are authenticated in runtime `SecurityConfig` and standalone test resolver support.
   - Confirm unauthenticated JSON envelope uses `error.code=UNAUTHORIZED`.
2. **Explicit map coordinates**
   - Verify `MapExploreRequest` requires both `mapX` and `mapY` and does not hardcode Seoul coordinates.
   - Verify service uses request coordinates only and does not read member-stored location.
3. **Privacy matrix**
   - Verify inaccessible notes are excluded, not masked.
   - Verify nickname is present for accessible notes.
   - Verify `profileImageUrl` is null for `NONE`, visible for `SELF`/`FRIEND` when stored.
   - Verify only `ACCEPTED` friendships count as `FRIEND`.
4. **One-image note reference**
   - Verify note create/update remains typed JSON.
   - Verify no multipart note mutation contract was introduced.
   - Verify schema and request validation allow at most one image reference per note.
5. **MinIO/presign**
   - Verify Java code binds `enjoytrip.minio.*`; no direct `System.getenv()` or `System.getProperty()` secrets.
   - Verify presign response includes `objectKey`, `uploadUrl`, `expiresAt`, and `publicUrl` or a documented read reference.
6. **Legacy compatibility**
   - Verify `/api/notes/nearby` keeps its legacy behavior unless explicitly changed.
   - Verify `/api/attractions/popular-nearby` remains intact.

## Required verification evidence

Use this block in completion reports after implementation integration:

```text
Verification:
- PASS ./gradlew :storage:db-core:generateJooq
- PASS ./gradlew :core:core-api:test
- PASS ./gradlew :storage:db-core:test
- PASS ./gradlew :core:core-api:test
- PASS ./gradlew :core:core-api:check
- PASS ./gradlew :support:auth:check
- PASS ./gradlew :core:core-api:check
- PASS rg -n "System\\.(getenv|getProperty)" external app/web (no MinIO secret reads)
- PASS HTTP JSON proof: GET /api/map/explore unauthenticated -> 401 UNAUTHORIZED envelope
- PASS HTTP JSON proof: POST /api/note-images/presigned-upload unauthenticated -> 401 UNAUTHORIZED envelope
- PASS HTTP JSON proof: map partial coordinate -> 400 envelope
- PASS HTTP JSON proof: missing map coordinates -> 400 BAD_REQUEST envelope
- PASS HTTP JSON proof: explicit map coordinates -> success envelope
- PASS HTTP JSON proof: SELF/FRIEND/NONE privacy matrix spot checks
- PASS HTTP JSON proof: presign response fields
```


## Subagent probe findings integrated

### Repository map probe

- Existing legacy nearby flow is split across `NearbySectionRequest`, `AttractionController`, `NoteController`, `AttractionService`, and `NoteStorageRepository`.
- `NearbySectionRequest` hardcodes Seoul fallback and should remain legacy-only.
- Representative-location fields already exist end-to-end in member request/domain/entity/repository/response code.
- The current schema still includes multi-row `note_photos`; Phase B must replace or constrain this to one image per note.
- No MinIO/S3 symbols were visible in `application.yml` or `external` before Phase B implementation integration.

### Review probe

- High-risk auth gap if `/api/map/explore` and `/api/note-images/presigned-upload` are not added to `SecurityConfig` and `TestAuthenticationPrincipalResolver`.
- High-risk privacy gap if map explore reuses the existing `Note` row return path instead of a map projection with `SELF | FRIEND | NONE` and profile-image masking.
- High-risk contract gap if note mutations do not add one validated image reference while staying typed JSON.
- Medium-risk verification gap if docs/tests stop at legacy nearby notes without runtime proof for map explore and presigned upload.

### Test probe

- Existing tests cover legacy note create/update/delete, legacy nearby fallback, JWT/security support, and SQL visibility predicate fragments.
- Missing tests include map endpoint auth, explicit-coordinate validation, SELF/FRIEND/NONE privacy shaping, non-ACCEPTED friendship exclusion, one-image schema validation, and MinIO config binding.
- Focused checks after implementation should include `:core:core-api:test`, `:storage:db-core:test`, `:core:core-api:test`, `:core:core-api:check`, `:support:auth:check`, `:core:core-api:check`, plus `:external:test` if MinIO client code is added.
