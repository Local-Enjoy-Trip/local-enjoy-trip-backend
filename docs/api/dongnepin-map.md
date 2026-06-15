# Dongnepin Map Explore API

> Phase B contract source: `.omx/plans/dongnepin-epic2-backend-plan.md` Phase B only.
> This document records the intended public HTTP contract and the runtime proof matrix for the Dongnepin map tab.

## Authentication

`GET /api/map/explore` is authenticated-only.

```http
GET /api/map/explore HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
```

Unauthenticated requests must return the standard envelope:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "인증이 필요합니다."
  }
}
```

## Query parameters

| Name | Required | Rule |
|---|---:|---|
| `mapX` | no | Longitude. If present, `mapY` must also be present. Valid range: -180 to 180. |
| `mapY` | no | Latitude. If present, `mapX` must also be present. Valid range: -90 to 90. |
| `radius` | no | Positive search radius in meters; implementation must cap it. |
| `limit` | no | Positive result limit; implementation must cap it. |
| `filter` | no | Optional map filter. Keep as a typed enum, not a raw string map. |
| `noteCategory` | no | Optional typed note category filter when note pins are requested. |

Rules:

- The DTO validates raw query shape only.
- Do not reuse the legacy home nearby Seoul fallback.
- If both coordinates are omitted, resolve the authenticated member representative location in the core service.
- If the member has no representative location, return `400 BAD_REQUEST` with message `대표 동네 위치를 먼저 설정하세요.`
- Explicit coordinates override the representative-location fallback.

## Successful response shape

The response must keep map-only privacy fields separate from CRUD `NoteResponse`.

```json
{
  "success": true,
  "data": {
    "center": {
      "latitude": 37.5665,
      "longitude": 126.978,
      "source": "REPRESENTATIVE_LOCATION"
    },
    "places": [
      {
        "id": 1,
        "title": "경복궁",
        "latitude": 37.5796,
        "longitude": 126.977,
        "firstImage": "https://example.test/gyeongbokgung.jpg"
      }
    ],
    "notes": [
      {
        "id": 10,
        "title": "오늘의 산책 기록",
        "category": "DAILY",
        "visibility": "PUBLIC",
        "latitude": 37.5665,
        "longitude": 126.978,
        "regionName": "서울 중구",
        "author": {
          "userId": "author1",
          "nickname": "동네친구",
          "profileImageUrl": null,
          "relationshipToViewer": "NONE"
        },
        "image": {
          "objectKey": "notes/author1/sample.jpg",
          "publicUrl": "http://localhost:9000/dongnepin-notes/notes/author1/sample.jpg"
        }
      }
    ]
  },
  "error": null
}
```

## Note privacy matrix

| viewer relationship | visible note visibility | author nickname | author profileImageUrl | inaccessible rows |
|---|---|---|---|---|
| `SELF` | `PUBLIC`, `FRIENDS`, `PRIVATE` | present | present when stored | excluded only if deleted/hidden |
| `FRIEND` | `PUBLIC`, `FRIENDS` | present | present when stored | `PRIVATE` excluded |
| `NONE` | `PUBLIC` | present | `null` | `FRIENDS`/`PRIVATE` excluded |

Additional requirements:

- Only `ACCEPTED` friendships count as `FRIEND`.
- `PENDING`, `REJECTED`, and `DELETED` friendships do not count as `FRIEND`.
- Inaccessible notes are excluded rather than returned with masked content.
- The storage query should project relationship/profile masking in one jOOQ path to avoid per-row friend/profile lookups.

## Required runtime JSON proof

Run against the web app with Postgres and MinIO when available.

```bash
# 401 security matcher proof
curl -s -i 'http://localhost:8080/api/map/explore' | tee /tmp/map-no-auth.txt
cat /tmp/map-no-auth.txt | sed -n '/^{/,$p' | jq '.success, .error.code, .error.message'

# partial coordinates -> 400
curl -s -H "Authorization: Bearer $JWT" \
  'http://localhost:8080/api/map/explore?mapX=126.9' \
  | jq '.success, .error.code, .error.message'

# representative-location fallback
curl -s -H "Authorization: Bearer $JWT_WITH_REPRESENTATIVE_LOCATION" \
  'http://localhost:8080/api/map/explore' \
  | jq '.success, .data.center'

# explicit coordinates override fallback
curl -s -H "Authorization: Bearer $JWT_WITH_REPRESENTATIVE_LOCATION" \
  'http://localhost:8080/api/map/explore?mapX=126.978&mapY=37.5665&radius=500&limit=20' \
  | jq '.success, .data.center'

# privacy matrix spot checks
curl -s -H "Authorization: Bearer $JWT_NONE_VIEWER" \
  'http://localhost:8080/api/map/explore?mapX=126.978&mapY=37.5665&radius=1000' \
  | jq '.data.notes[] | {id, visibility, relationship: .author.relationshipToViewer, profileImageUrl: .author.profileImageUrl}'
```
