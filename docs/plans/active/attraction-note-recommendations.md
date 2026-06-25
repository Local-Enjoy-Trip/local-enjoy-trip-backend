# 장소·쪽지 선호도 기반 추천 API

## 배경

코스 추천(`GET /api/courses/recommendations`)은 `member_profile_embeddings` ↔ `course_embeddings` 코사인 유사도로 후보를 뽑아 리랭킹하는 방식으로 구현되어 있다. 장소(`attraction_embeddings`, 1536dim)·쪽지(`note_embeddings`, 1536dim) 임베딩 테이블과 `member_profile_embeddings`가 이미 존재하므로 동일한 패턴을 적용한다.

**목표 엔드포인트:**
- `GET /api/attractions/recommendations` (인증 필수)
- `GET /api/notes/recommendations` (인증 필수)

---

## Step 1 — `MemberProfileEmbeddingMapper`에 공통 메서드 추가

`hasMemberProfileEmbedding(memberId)` 가 현재 `CourseMapper`에 위치하지만 논리적으로는 `MemberProfileEmbeddingMapper`에 속한다. 두 서비스(AttractionService, NoteService)가 공통으로 사용할 수 있도록 여기에 추가한다.

**변경 파일:**
- `storage/db-core/src/main/java/…/mapper/MemberProfileEmbeddingMapper.java`
- `storage/db-core/src/main/resources/mybatis/mapper/MemberProfileEmbeddingMapper.xml`

```java
boolean hasMemberProfileEmbedding(@Param("memberId") Long memberId);
```

```xml
<select id="hasMemberProfileEmbedding" resultType="boolean">
    select exists(
        select 1
        from member_profile_embeddings
        where member_id = #{memberId}
          and status = 'EMBEDDED'
          and embedding is not null
    )
</select>
```

---

## Step 2 — `AttractionMapper` + XML에 추천·폴백 쿼리 추가

**변경 파일:**
- `storage/db-core/src/main/java/…/mapper/AttractionMapper.java`
- `storage/db-core/src/main/resources/mybatis/mapper/AttractionMapper.xml`

추천 쿼리 (pgvector 코사인 유사도):

```java
List<AttractionSearchRecord> findCandidatesByMemberProfile(
    @Param("memberId") Long memberId,
    @Param("limit") int limit,
    @Param("memberId") Long viewerMemberId
);
```

```sql
WITH member_vec AS (
    SELECT embedding
    FROM member_profile_embeddings
    WHERE member_id = #{memberId}
      AND status = 'EMBEDDED'
      AND embedding IS NOT NULL
)
SELECT <include refid="searchRowColumns"/>, ae.embedding <=> mv.embedding AS similarityDistance
FROM (
    SELECT <include refid="baseColumns"/>, NULL AS distanceMeters
    FROM attractions
    WHERE <include refid="publicAttractionPredicate"/>
) a
JOIN attraction_embeddings ae ON ae.attraction_id = a.id
CROSS JOIN member_vec mv
LEFT JOIN attraction_popularity_stats stats ON stats.attraction_id = a.id
LEFT JOIN attraction_saves s ON s.attraction_id = a.id AND s.member_id = #{memberId}
LEFT JOIN member_attraction_ratings r ON r.attraction_id = a.id AND r.member_id = #{memberId}
WHERE ae.status = 'EMBEDDED'
ORDER BY similarityDistance ASC
LIMIT #{limit}
```

폴백 쿼리 (프로필 임베딩 없을 때 인기 장소):

```java
List<AttractionSearchRecord> findTopBySaveCount(
    @Param("limit") int limit,
    @Param("memberId") Long viewerMemberId
);
```

---

## Step 3 — `NoteMapper` + XML에 추천·폴백 쿼리 추가

**변경 파일:**
- `storage/db-core/src/main/java/…/mapper/NoteMapper.java`
- `storage/db-core/src/main/resources/mybatis/mapper/NoteMapper.xml`

추천 쿼리:

```java
List<NoteRecord> findCandidatesByMemberProfile(
    @Param("memberId") Long memberId,
    @Param("limit") int limit
);
```

```sql
WITH member_vec AS (
    SELECT embedding
    FROM member_profile_embeddings
    WHERE member_id = #{memberId}
      AND status = 'EMBEDDED'
      AND embedding IS NOT NULL
)
SELECT <noteColumns>
FROM notes n
JOIN note_embeddings ne ON ne.note_id = n.id
CROSS JOIN member_vec mv
WHERE n.deleted_at IS NULL
  AND n.visibility = 'PUBLIC'
  AND ne.status = 'EMBEDDED'
ORDER BY ne.embedding <=> mv.embedding ASC
LIMIT #{limit}
```

폴백 쿼리 (최근 PUBLIC 쪽지):

```java
List<NoteRecord> findRecentPublic(@Param("limit") int limit);
```

---

## Step 4 — `AttractionService`에 `findRecommendations` 추가

**변경 파일:** `core/core-api/src/main/java/…/service/AttractionService.java`

```java
public List<Attraction> findRecommendations(Long memberId, int limit) {
    if (!memberProfileEmbeddingMapper.hasMemberProfileEmbedding(memberId)) {
        return attractionMapper.findTopBySaveCount(limit, memberId)
                .stream().map(this::toAttraction).toList();
    }
    return attractionMapper.findCandidatesByMemberProfile(memberId, limit, memberId)
            .stream().map(this::toAttraction).toList();
}
```

`MemberProfileEmbeddingMapper` 필드 주입 추가 필요.

---

## Step 5 — `NoteService`에 `findRecommendations` 추가

**변경 파일:** `core/core-api/src/main/java/…/service/NoteService.java`

```java
public List<Note> findRecommendations(Long memberId, int limit) {
    if (!memberProfileEmbeddingMapper.hasMemberProfileEmbedding(memberId)) {
        return noteMapper.findRecentPublic(limit)
                .stream().map(this::toNote).toList();
    }
    return noteMapper.findCandidatesByMemberProfile(memberId, limit)
            .stream().map(this::toNote).toList();
}
```

`MemberProfileEmbeddingMapper` 필드 주입 추가 필요.

---

## Step 6 — Request DTO 2개 추가

**신규 파일:**
- `core/core-api/src/main/java/…/dto/request/AttractionRecommendationRequest.java`
- `core/core-api/src/main/java/…/dto/request/NoteRecommendationRequest.java`

```java
public record AttractionRecommendationRequest(
    @Positive @Max(50) Integer limit
) {
    private static final int DEFAULT_LIMIT = 10;

    public int resolvedLimit() {
        return limit == null ? DEFAULT_LIMIT : limit;
    }
}
```

`NoteRecommendationRequest`도 동일 구조.

---

## Step 7 — `AttractionApi` + `AttractionController`에 엔드포인트 추가

**변경 파일:**
- `core/core-api/src/main/java/…/web/api/AttractionApi.java`
- `core/core-api/src/main/java/…/web/controller/AttractionController.java`

```
GET /api/attractions/recommendations?limit=10
Authorization: Bearer <token>
```

응답: `ApiResponse<AttractionsResponse>`

---

## Step 8 — `NoteApi` + `NoteController`에 엔드포인트 추가

**변경 파일:**
- `core/core-api/src/main/java/…/web/api/NoteApi.java`
- `core/core-api/src/main/java/…/web/controller/NoteController.java`

```
GET /api/notes/recommendations?limit=10
Authorization: Bearer <token>
```

응답: `ApiResponse<NotesResponse>`

---

## Step 9 — 테스트 추가

### db-core (H2)
- pgvector 의존 SQL은 H2에서 실행 불가 → `@Tag("container")` container 테스트로 분리
- `findTopBySaveCount`, `findRecentPublic`은 H2 테스트로 커버

### core-api (MockMvc)
- `AttractionController`: `GET /api/attractions/recommendations` 성공·인증 실패 케이스
- `NoteController`: `GET /api/notes/recommendations` 성공·인증 실패 케이스

---

## DB 마이그레이션

**불필요.** `attraction_embeddings`·`note_embeddings`·`member_profile_embeddings` 테이블이 이미 존재한다.

---

## 변경 파일 요약

| 레이어 | 파일 |
|--------|------|
| db-core mapper | `MemberProfileEmbeddingMapper.java`, `AttractionMapper.java`, `NoteMapper.java` |
| db-core XML | `MemberProfileEmbeddingMapper.xml`, `AttractionMapper.xml`, `NoteMapper.xml` |
| core-api service | `AttractionService.java`, `NoteService.java` |
| core-api dto (신규) | `AttractionRecommendationRequest.java`, `NoteRecommendationRequest.java` |
| core-api api | `AttractionApi.java`, `NoteApi.java` |
| core-api controller | `AttractionController.java`, `NoteController.java` |
| core-api test | `AttractionControllerTest.java`, `NoteControllerTest.java` |
| db-core test | container 테스트 케이스 추가 |
