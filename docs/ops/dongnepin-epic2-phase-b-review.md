# Dongnepin Epic 2 Phase B Review Notes

> Worker-3 scope: review and documentation for `.omx/plans/dongnepin-epic2-backend-plan.md` Phase B.
> Timestamp: 2026-06-15.

## Reviewed source-of-truth

- `backend/CONSTITUTION.md` from session-start rules, with `backend/RULES.md` covered by the Stop governance check.
- Applicable module rules:
  - `backend/AGENTS.md`
  - `backend/app/web/AGENTS.md`
  - `backend/core/AGENTS.md`
  - `backend/storage/AGENTS.md`
  - `backend/external/AGENTS.md`
- Phase B section of `.omx/plans/dongnepin-epic2-backend-plan.md`.

## Boundary checklist for implementation review

- `backend/app/web` may own controllers, request/response DTOs, OpenAPI contracts, validation, and response envelopes.
- `backend/app/web/src/main` must not import `com.ssafy.enjoytrip.storage.*`.
- `backend/core` owns representative-location fallback decision, business error, commands/projections, and service flow.
- `backend/storage` owns jOOQ/PostGIS queries, visibility/friend relationship projection, note image persistence, and migration.
- `backend/external` owns MinIO/S3-compatible presigned upload client and config binding.
- `backend/support/auth` owns security matcher protection for the new authenticated routes.

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
2. **Representative-location fallback**
   - Verify `MapExploreRequest` only validates raw query shape and does not hardcode Seoul fallback.
   - Verify service uses explicit coordinates first, then authenticated member representative location.
   - Verify missing representative location uses a specific `BAD_REQUEST` business error message: `대표 동네 위치를 먼저 설정하세요.`
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
- PASS ./gradlew :backend:storage:generateJooq
- PASS ./gradlew :backend:core:test
- PASS ./gradlew :backend:storage:test
- PASS ./gradlew :backend:app:web:test
- PASS ./gradlew :backend:app:web:check
- PASS ./gradlew :backend:support:auth:check
- PASS ./gradlew :backend:app:check
- PASS rg -n "System\\.(getenv|getProperty)" backend/external backend/app/web (no MinIO secret reads)
- PASS HTTP JSON proof: GET /api/map/explore unauthenticated -> 401 UNAUTHORIZED envelope
- PASS HTTP JSON proof: POST /api/note-images/presigned-upload unauthenticated -> 401 UNAUTHORIZED envelope
- PASS HTTP JSON proof: map partial coordinate -> 400 envelope
- PASS HTTP JSON proof: missing representative location -> 400 BAD_REQUEST envelope
- PASS HTTP JSON proof: representative fallback and explicit coordinate override
- PASS HTTP JSON proof: SELF/FRIEND/NONE privacy matrix spot checks
- PASS HTTP JSON proof: presign response fields
```


## Subagent probe findings integrated

### Repository map probe

- Existing legacy nearby flow is split across `NearbySectionRequest`, `AttractionController`, `NoteController`, `AttractionService`, and `NoteStorageRepository`.
- `NearbySectionRequest` hardcodes Seoul fallback and should remain legacy-only.
- Representative-location fields already exist end-to-end in member request/domain/entity/repository/response code.
- The current schema still includes multi-row `note_photos`; Phase B must replace or constrain this to one image per note.
- No MinIO/S3 symbols were visible in `application.yml` or `backend/external` before Phase B implementation integration.

### Review probe

- High-risk auth gap if `/api/map/explore` and `/api/note-images/presigned-upload` are not added to `SecurityConfig` and `TestAuthenticationPrincipalResolver`.
- High-risk privacy gap if map explore reuses the existing `Note` row return path instead of a map projection with `SELF | FRIEND | NONE` and profile-image masking.
- High-risk contract gap if note mutations do not add one validated image reference while staying typed JSON.
- Medium-risk verification gap if docs/tests stop at legacy nearby notes without runtime proof for map explore and presigned upload.

### Test probe

- Existing tests cover legacy note create/update/delete, legacy nearby fallback, JWT/security support, and SQL visibility predicate fragments.
- Missing tests include map endpoint auth, representative-location fallback/error, SELF/FRIEND/NONE privacy shaping, non-ACCEPTED friendship exclusion, one-image schema validation, and MinIO config binding.
- Focused checks after implementation should include `:backend:core:test`, `:backend:storage:test`, `:backend:app:web:test`, `:backend:app:web:check`, `:backend:support:auth:check`, `:backend:app:check`, plus `:backend:external:test` if MinIO client code is added.
