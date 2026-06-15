# Dongnepin note image MinIO setup

`POST /api/note-images/presigned-upload` issues a time-limited PUT URL for one note image reference.
The note mutation remains typed JSON; clients upload the image to MinIO first, then pass one image
reference in the note create/update JSON body.

Local defaults are provided by `.env.example`:

```properties
ENJOYTRIP_MINIO_ENDPOINT=http://localhost:9000
ENJOYTRIP_MINIO_BUCKET=dongnepin-notes
ENJOYTRIP_MINIO_ACCESS_KEY=minioadmin
ENJOYTRIP_MINIO_SECRET_KEY=minioadmin
ENJOYTRIP_MINIO_REGION=ap-northeast-2
ENJOYTRIP_MINIO_PUBLIC_BASE_URL=http://localhost:9000/dongnepin-notes
```

Run local dependencies:

```bash
docker compose up -d db redis minio minio-bootstrap
```

The presign response contains `objectKey`, `uploadUrl`, `expiresAt`, and `publicUrl`.
The `publicUrl` is a read reference built from `enjoytrip.minio.public-base-url` and the object key.
