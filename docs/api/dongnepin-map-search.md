# Dongnepin Map Search API

지도 검색 API는 키워드를 통해 장소(관광지 제목 기준) 및 쪽지(본문 기준)를 검색하여 하나의 통합 리스트로 조합 및 정렬해 반환하는 API입니다.

## Authentication

`GET /api/map/search`는 인증된 사용자만 호출할 수 있습니다. (authenticated-only)

```http
GET /api/map/search HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
```

인증되지 않은 요청 시 다음과 같은 표준 에러 응답을 반환합니다:

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

## Query Parameters

| 이름 | 필수 여부 | 유효성 검증 규칙 | 설명 |
|---|:---:|---|---|
| `keyword` | **필수** | `@NotBlank`, 길이 1~50자, 공백 제거됨 | 검색 키워드 |
| `mapX` | **필수** | 경도 (Longitude). `-180` ~ `180` 범위의 실수. | 중심경도 |
| `mapY` | **필수** | 위도 (Latitude). `-90` ~ `90` 범위의 실수. | 중심위도 |
| `radius` | 선택 | 양수 (Positive). 단위: 미터 (m). | 검색 반경. 누락 시 전역 범위 검색. |
| `target` | 선택 | `PLACE`, `NOTE`, `ALL` 중 하나 (기본값: `ALL`) | 검색 대상 지정 |
| `noteCategory` | 선택 | `TIP`, `DAILY`, `BOOK` 등 `NoteCategory` 열거형 값 | 쪽지 검색 카테고리 필터 |
| `limit` | 선택 | 양수 (Positive). 서비스단에서 최대 `50`으로 제한. | 최대 반환 개수 (카테고리당 최대 `50` 건) |

### Validation Rules

- `keyword`가 누락되거나 빈 문자열/공백만 전달된 경우: `400 Bad Request` 에러 반환.
- `mapX`, `mapY` 중 하나라도 누락되거나 범위를 벗어난 경우: `400 Bad Request` 에러 반환.
- 잘못된 파라미터 전달 시의 에러 응답 포맷:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "BAD_REQUEST",
    "message": "올바르지 않은 요청 파라미터입니다."
  }
}
```

## Response Schema

응답은 `ApiResponse<List<MapPin>>` 형태로 반환되며, `center`, `keyword`, `radiusMeters` 등 불필요한 메타데이터 필드나 `results`와 같은 wrapper 객체 없이 다형성 JSON 배열 목록으로 직접 구성됩니다.

각 핀(Pin)은 공통적으로 `"type"` 필드를 가지며, 이 필드를 통해 `PLACE`(장소) 또는 `NOTE`(쪽지)를 구분할 수 있습니다.

### Successful Response Example (`200 OK`)

```json
{
  "success": true,
  "data": [
    {
      "type": "PLACE",
      "id": 12,
      "title": "경복궁",
      "address": "서울특별시 종로구 사직로 161",
      "latitude": 37.5796,
      "longitude": 126.9770,
      "imageUrl": "https://example.test/gyeongbokgung.jpg",
      "contentTypeId": "12",
      "distanceMeters": 100.5,
      "saved": true,
      "saveCount": 42,
      "ratingAverage": 4.8,
      "ratingCount": 150,
      "matchTier": 0
    },
    {
      "type": "NOTE",
      "id": 204,
      "title": "경복궁 산책 후기",
      "category": "TIP",
      "visibility": "PUBLIC",
      "latitude": 37.5802,
      "longitude": 126.9775,
      "regionName": "서울 종로구",
      "distanceMeters": 120.3,
      "imageObjectKey": "notes/1/abcd-efgh.jpg",
      "authorNickname": "경복궁지기",
      "authorProfileImageUrl": null,
      "relationshipToViewer": "NONE",
      "createdAt": "2026-06-20T14:30:00",
      "matchTier": 1
    }
  ],
  "error": null
}
```

## Privacy & Masking Matrix (Note Privacy)

쪽지 검색 시 조회 조건 및 개인정보 마스킹 로직은 기존 지도 탐색(`/api/map/explore`)과 동일한 규칙을 따르며, 공통 SQL 프래그먼트(`<sql id="mapPinColumns">`)를 공유하여 일관되게 보장됩니다.

| 조회자 관계 (`viewer relationship`) | 열람 가능한 쪽지 공개 범위 (`visibility`) | 작성자 닉네임 (`authorNickname`) | 작성자 프로필 이미지 (`profileImageUrl`) | 비공개 및 차단 대상 (`inaccessible rows`) |
|---|---|---|---|---|
| `SELF` (본인) | `PUBLIC`, `FRIENDS`, `PRIVATE` | 노출 | 노출 (있는 경우) | 없음 (삭제/숨김 글 제외) |
| `FRIEND` (친구) | `PUBLIC`, `FRIENDS` | 노출 | 노출 (있는 경우) | `PRIVATE` 제외 |
| `NONE` (타인) | `PUBLIC` | 노출 | `null` | `FRIENDS`, `PRIVATE` 제외 |

* 주의: `FRIEND` 등급은 오직 친구 관계 수락이 완료된(`ACCEPTED`) 상태일 때만 인정됩니다. (`PENDING`, `REJECTED`, `DELETED` 상태는 `NONE`으로 취급)

## Ranking and Merging Spec

1. **정확도 우선 정렬 (Tier)**:
   - **Tier 0 (Exact Match)**: 검색어와 제목(장소) 또는 본문(쪽지)이 완전히 일치하는 경우 (대소문자 구분 없음, Whole-field equality)
   - **Tier 1 (Contains Match)**: 검색어가 부분 일치하는 경우 (`ILIKE '%keyword%'`)
2. **지리적 거리 정렬 (Distance)**:
   - 동일한 티어 내에서는 요청한 좌표(`mapX`, `mapY`)로부터의 물리적 거리가 가까운 순서(`distanceMeters` 오름차순)로 정렬됩니다.
3. **병합 및 정렬 단계**:
   - `MapSearchService`에서 장소 검색 목록(`PlaceMapPin`)과 쪽지 검색 목록(`NoteMapPin`)을 각각 조회한 후 하나의 리스트로 병합합니다.
   - Java Comparator를 통해 `matchTier` 오름차순 -> `distanceMeters` 오름차순 순서로 최종 통합 정렬을 수행합니다.

## Performance Analysis & EXPLAIN ANALYZE Recipes

인덱싱 튜닝 및 pg_trgm 적용 전, 풀 스캔(sequential scan) 상태에서의 기준선(baseline) 성능 측정을 위해 다음 쿼리를 활용해 실행 계획을 분석합니다.

### 1. 쪽지 검색 실행 계획 프로빙 (Notes Search Execution Plan)

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT n.id, 
       n.title,
       n.category,
       n.visibility,
       n.latitude,
       n.longitude,
       n.region_name as regionName,
       n.image_object_key as imageObjectKey,
       n.author_member_id as authorMemberId,
       coalesce(m.nickname, m.name, m.email) as authorNickname,
       ST_Distance(n.location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) as distanceMeters
FROM notes n
JOIN members m ON m.id = n.author_member_id
WHERE n.status = 'ACTIVE'
  -- 개인정보 권한 액세스 필터 예시 (PUBLIC 및 본인 글만)
  AND (n.visibility = 'PUBLIC' OR n.author_member_id = :viewerMemberId)
  -- 키워드 본문 contains 매칭
  AND n.content ILIKE concat('%', :escapedKeyword, '%') ESCAPE '\'
  -- 반경 공간 필터 (선택 사항)
  -- AND ST_DWithin(n.location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radiusMeters)
ORDER BY (CASE WHEN lower(n.content) = lower(:keyword) then 0 else 1 end) ASC,
         distanceMeters ASC,
         n.created_at DESC,
         n.id DESC
LIMIT 50;
```

### 2. 장소 검색 실행 계획 프로빙 (Places Search Execution Plan)

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT a.id,
       a.title,
       a.latitude,
       a.longitude,
       ST_Distance(a.location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) as distanceMeters,
       (CASE WHEN lower(a.title) = lower(:keyword) then 0 else 1 end) as matchTier
FROM attractions a
WHERE a.status = 'ACTIVE'
  -- 키워드 제목 contains 매칭
  AND a.title ILIKE concat('%', :escapedKeyword, '%') ESCAPE '\'
  -- 반경 공간 필터 (선택 사항)
  -- AND ST_DWithin(a.location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radiusMeters)
ORDER BY matchTier ASC,
         distanceMeters ASC,
         a.id ASC
LIMIT 50;
```
