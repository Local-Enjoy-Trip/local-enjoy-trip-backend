# Note Images API

> Phase B contract source: `.omx/plans/dongnepin-epic2-backend-plan.md` Phase B only.

## Overview

Dongnepin note image upload uses a typed JSON presign endpoint. Note create/update must remain JSON and must not become multipart/form-data.

- One note accepts at most one image reference.
- The image reference is stored as object metadata such as `imageObjectKey` plus a read URL/reference.
- MinIO/S3-compatible configuration is bound from `enjoytrip.minio.*`; Java code must not read secrets with `System.getenv()` or `System.getProperty()` directly.

## Create presigned upload URL

```http
POST /api/note-images/presigned-upload HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
Content-Type: application/json

{
  "contentType": "image/jpeg",
  "fileExtension": "jpg"
}
```

Expected response:

```json
{
  "success": true,
  "data": {
    "objectKey": "notes/ssafy/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg",
    "uploadUrl": "http://localhost:9000/dongnepin-notes/notes/ssafy/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg?...",
    "expiresAt": "2026-06-15T05:10:00Z",
    "publicUrl": "http://localhost:9000/dongnepin-notes/notes/ssafy/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg"
  },
  "error": null
}
```

Unauthenticated requests must return `401` with `error.code=UNAUTHORIZED`.

## Note mutation reference

After uploading to the returned `uploadUrl`, note create/update may reference the object through a single image field, for example:

```json
{
  "title": "오늘의 산책",
  "content": "동네 한 바퀴 기록",
  "category": "DAILY",
  "visibility": "PUBLIC",
  "latitude": 37.5665,
  "longitude": 126.978,
  "regionName": "서울 중구",
  "imageObjectKey": "notes/ssafy/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg"
}
```

The backend must reject multiple image references for one note. The CRUD response should not be expanded with map-only privacy fields; map note cards use a separate response DTO.

## Runtime JSON proof

```bash
# 401 security matcher proof
curl -s -i -X POST 'http://localhost:8080/api/note-images/presigned-upload' \
  -H 'Content-Type: application/json' \
  -d '{"contentType":"image/jpeg","fileExtension":"jpg"}' \
  | tee /tmp/note-image-presign-no-auth.txt
cat /tmp/note-image-presign-no-auth.txt | sed -n '/^{/,$p' | jq '.success, .error.code, .error.message'

# authenticated presign proof
curl -s -X POST 'http://localhost:8080/api/note-images/presigned-upload' \
  -H "Authorization: Bearer $JWT" \
  -H 'Content-Type: application/json' \
  -d '{"contentType":"image/jpeg","fileExtension":"jpg"}' \
  | jq '.success, .data.objectKey, .data.uploadUrl, .data.expiresAt, .data.publicUrl'
```
