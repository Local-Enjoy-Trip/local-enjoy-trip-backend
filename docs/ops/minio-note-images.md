# Local MinIO for Dongnepin Note Images

> Phase B contract source: `.omx/plans/dongnepin-epic2-backend-plan.md` Phase B only.

## Required Spring properties

`backend/app/web/src/main/resources/application.yml` should expose MinIO through Spring configuration binding:

```yaml
enjoytrip:
  minio:
    endpoint: ${ENJOYTRIP_MINIO_ENDPOINT:http://localhost:9000}
    bucket: ${ENJOYTRIP_MINIO_BUCKET:dongnepin-notes}
    access-key: ${ENJOYTRIP_MINIO_ACCESS_KEY:minioadmin}
    secret-key: ${ENJOYTRIP_MINIO_SECRET_KEY:minioadmin}
    region: ${ENJOYTRIP_MINIO_REGION:ap-northeast-2}
    public-base-url: ${ENJOYTRIP_MINIO_PUBLIC_BASE_URL:http://localhost:9000/dongnepin-notes}
```

Rules:

- Keep MinIO/S3 client code in `backend/external`.
- Keep the HTTP presign endpoint thin in `backend/app/web`.
- Do not call `System.getenv()` or `System.getProperty()` from Java for MinIO secrets.
- Add a config binding smoke test for `enjoytrip.minio.*`.

## Local environment variables

```bash
export ENJOYTRIP_MINIO_ENDPOINT=http://localhost:9000
export ENJOYTRIP_MINIO_BUCKET=dongnepin-notes
export ENJOYTRIP_MINIO_ACCESS_KEY=minioadmin
export ENJOYTRIP_MINIO_SECRET_KEY=minioadmin
export ENJOYTRIP_MINIO_REGION=ap-northeast-2
export ENJOYTRIP_MINIO_PUBLIC_BASE_URL=http://localhost:9000/dongnepin-notes
```

## Docker Compose expectations

The local Compose stack should provide a MinIO service on:

- API: `http://localhost:9000`
- Console: `http://localhost:9001`
- Bucket: `dongnepin-notes`

If the bucket is not provisioned automatically, create it before runtime proof:

```bash
mc alias set enjoytrip-local http://localhost:9000 minioadmin minioadmin
mc mb --ignore-existing enjoytrip-local/dongnepin-notes
mc anonymous set download enjoytrip-local/dongnepin-notes
```

## Verification checklist

```bash
./gradlew :backend:external:test
./gradlew :backend:app:web:test
./gradlew :backend:app:web:check
rg -n "System\\.(getenv|getProperty)" backend/external backend/app/web
```

Runtime proof should include:

- `POST /api/note-images/presigned-upload` without JWT -> `401 UNAUTHORIZED` envelope.
- Authenticated presign response includes `objectKey`, `uploadUrl`, `expiresAt`, and `publicUrl` or a documented read reference.
- Optional real PUT upload to `uploadUrl` succeeds with the requested `Content-Type`.
