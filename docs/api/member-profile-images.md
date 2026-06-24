# Member Profile Images API

> Source plan: `.omx/plans/member-profile-image-presigned-upload-plan.md`

## Overview

Member profile images use MinIO/S3-compatible presigned `PUT` upload. The backend stores both the canonical
`objectKey` and the server-derived public URL, but public member responses expose only `profileImageUrl`.

- Signup remains URL-only compatible and does not require a presigned upload flow.
- `PUT /api/members/me` updates general member fields only; profile-image mutation is handled here.
- The server generates object keys under `profiles/{authenticatedMemberId}/` and rejects foreign prefixes.
- Clients never send `publicUrl` to persist profile images; the backend recalculates it from `enjoytrip.minio.public-base-url`.

## Create presigned upload URL

```http
POST /api/members/me/profile-image/presigned-upload HTTP/1.1
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
    "objectKey": "profiles/11/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg",
    "uploadUrl": "http://localhost:9000/dongnepin-notes/profiles/11/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg?...",
    "expiresAt": "2026-06-22T05:10:00Z",
    "publicUrl": "http://localhost:9000/dongnepin-notes/profiles/11/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg"
  },
  "error": null
}
```

Rules:

- Authentication is required; unauthenticated requests return `401` with `error.code=S401`.
- `contentType` must be `image/*`; non-image requests return `400` with `error.code=C400`.
- Returned `objectKey` always starts with `profiles/{authenticatedMemberId}/`.

## Save or replace profile image

After uploading the file to the returned `uploadUrl`, persist the object key:

```http
PUT /api/members/me/profile-image HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
Content-Type: application/json

{
  "objectKey": "profiles/11/018f0a2a-55c1-7a7c-b3f5-fb2ed9e6b51b.jpg",
  "contentType": "image/jpeg"
}
```

Expected response:

```json
{
  "success": true,
  "data": null,
  "error": null
}
```

Rules:

- Authentication is required; unauthenticated requests return `401` with `error.code=S401`.
- `objectKey` must start with `profiles/{authenticatedMemberId}/`; foreign prefixes return `400`.
- `contentType` must be `image/*`; non-image requests return `400`.
- The database stores `profile_image_object_key` and `profile_image_url` together for this endpoint.
- `GET /api/members/me` returns `profileImageUrl` only and does not expose `profileImageObjectKey`.
