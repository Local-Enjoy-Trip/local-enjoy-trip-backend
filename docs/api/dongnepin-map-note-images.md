# Dongnepin Map Explore and Note Image Upload API

## `GET /api/map/explore`

Authenticated users can fetch map pins for places and accessible notes.

Query parameters:

- `mapX`, `mapY`: required longitude/latitude pair. Missing either value returns a `400` JSON error.
- `radius`: optional search radius in meters. Default is `1000`, maximum is `5000`.
- `limit`: optional per-type limit. Default is `50`, maximum is `100`.
- `filter`: optional `ALL`, `PLACE`, `NOTE`, or `FRIEND`.
- `noteCategory`: optional note category filter for note pins.

Center resolution:

1. The request `mapX` and `mapY` coordinates are used.
2. The API does not fallback to member-stored representative location.
3. Missing coordinates return a `400 BAD_REQUEST` JSON error.

Note privacy projection:

- `SELF`: own `PUBLIC`, `FRIENDS`, and `PRIVATE` notes are visible; profile image is visible.
- `FRIEND`: accepted friends' `PUBLIC` and `FRIENDS` notes are visible; profile image is visible.
- `NONE`: only `PUBLIC` notes are visible; author nickname remains visible and profile image is `null`.
- Inaccessible notes are excluded from the response.

## `POST /api/note-images/presigned-upload`

Authenticated users can request a MinIO/S3-compatible presigned PUT upload URL.

Request JSON:

```json
{
  "contentType": "image/jpeg",
  "fileExtension": "jpg"
}
```

Response JSON fields:

- `objectKey`: canonical single image reference to store in note create/update JSON.
- `uploadUrl`: time-limited PUT URL.
- `expiresAt`: expiration timestamp.
- `publicUrl`: read/reference URL for the object key.

Notes continue to use typed JSON mutations and accept at most one image reference through
`imageObjectKey`; multipart note mutations are not part of this contract.

## Local MinIO

`docker-compose.yml` includes `minio` and `minio-bootstrap` services. Defaults are exposed through
`.env.example` and bound in the app as `enjoytrip.minio.*`.

Important defaults:

- API endpoint: `http://localhost:19000`
- Console: `http://localhost:9001`
- Bucket: `dongnepin-notes`
- Public base URL: `http://localhost:19000/dongnepin-notes`
