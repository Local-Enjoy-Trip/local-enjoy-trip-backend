# AI 코스 생성 기능 계획

## 사용자 입력 (코스 생성 폼)

| 질문 | 선택지 | 비고 |
|---|---|---|
| 어떤 동네로 떠날까요? | 동네명 또는 sidoCode + gugunCode | 필수 |
| 누구와 떠나나요? | 혼자 / 친구와 / 연인과 / 아이와 / 부모님과 / 반려동물과 | 단일 선택 |
| 어떤 하루를 보내고 싶나요? | 동네 맛집 / 감성 카페 / 로컬 산책 / 문화 혹은 전시 / 자연 속 휴식 / 사진 명소 / 시장 골목 / 쇼핑 | 복수 선택 |
| 어떤 속도로 둘러볼까요? | 여유롭게(3곳) / 알맞게(4곳 안팎) / 알차게(5곳 안팎) | 단일 선택, 장소 수 결정 |

이 4가지 입력만으로 코스 생성이 가능하다. 유저 프로필 임베딩이 있으면 참조 코스 검색에 추가 활용한다.

---

## RAG 파이프라인

```
사용자 입력 (동네, 동행, 테마, 속도)
    │
    ▼ [Retrieval — 2단계]
① 취향 텍스트 즉석 임베딩 (companion + themes + pace 조합)
    └─→ attraction_embeddings 유사도 검색 → 관광지 후보 20개 (sido_code 필터 적용)

② (선택) 유저 프로필 임베딩이 있으면
    └─→ course_embeddings 유사도 검색 → 참조 코스 3~5개
         (findCandidatesByMemberProfile 쿼리 이미 존재)
    │
    ▼ [Augmentation]
③ 프롬프트 구성:
    - 요청 조건: 동네, 동행, 테마, 속도(→ 장소 수)
    - 후보 관광지 목록 20개 (id, 제목, 주소, 분류, 개요 요약)
    - (있으면) 유저 취향 설명 (profile_description)
    - (있으면) 참조 코스 3~5개 (제목 + 정류장 나열)
    │
    ▼ [Generation]
④ LLM → JSON: { title, attractionIds[], reason }
    장소 수: 속도 값에 따라 3 / 4 / 5개로 제한
    │
    ▼
⑤ 미리보기 응답 반환 (DB 저장 안 함)
    │
    ▼ [사용자가 저장 버튼 클릭]
⑥ 기존 POST /api/courses 로 저장
```

---

## API 설계

### 생성 (미리보기)

```
POST /api/courses/ai-generate
Authorization: Bearer {jwt}
```

**요청 `AiCourseGenerateRequest`:**
```json
{
  "sidoCode": 1,
  "gugunCode": 11680,
  "companion": "WITH_PARTNER",
  "themes": ["CAFE", "PHOTO"],
  "pace": "RELAXED"
}
```

**응답 `AiCoursePreviewResponse`:**
```json
{
  "success": true,
  "data": {
    "title": "연인과 떠나는 강남 감성 카페 투어",
    "reason": "...",
    "stops": [
      { "attractionId": 123, "title": "카페 OOO", "addr1": "...", "firstImage": "..." },
      ...
    ]
  }
}
```

### 저장

기존 `POST /api/courses` 재사용. 클라이언트가 미리보기 응답의 attractionId 목록을 포함해 요청한다.

---

## 구현 대상 파일

| 모듈 | 파일 | 변경 유형 | 설명 |
|---|---|---|---|
| `storage:db-core` | `AttractionMapper.xml` | 쿼리 추가 | 취향 임베딩 기반 관광지 유사도 검색 |
| `storage:db-core` | `AttractionMapper.java` | 메서드 추가 | `findCandidatesByPreferenceEmbedding()` |
| `storage:db-core` | `AttractionEmbeddingCandidateRecord.java` | 신규 | 쿼리 결과 레코드 (id, title, addr1, contentTypeId, overview) |
| `external` | `AiCourseGenerationClient.java` | 신규 | 프롬프트 구성 + LLM 호출 + JSON 파싱 |
| `external` | `AiCourseGenerationInput.java` | 신규 | LLM 입력 record |
| `external` | `AiCourseGenerationResult.java` | 신규 | LLM 결과 record (title, attractionIds, reason) |
| `core:core-api` | `AiCourseGenerationService.java` | 신규 | 취향 임베딩 → Retrieval → LLM → 미리보기 반환 |
| `core:core-api` | `AiCourseController.java` | 신규 | POST /api/courses/ai-generate |
| `core:core-api` | `AiCourseGenerateRequest.java` | 신규 | 요청 DTO |
| `core:core-api` | `AiCoursePreviewResponse.java` | 신규 | 응답 DTO |
| `core:core-api` | `Companion.java` | 신규 enum | ALONE / WITH_FRIEND / WITH_PARTNER / WITH_CHILD / WITH_PARENTS / WITH_PET |
| `core:core-api` | `CourseTheme.java` | 신규 enum | FOOD / CAFE / WALK / CULTURE / NATURE / PHOTO / MARKET / SHOPPING |
| `core:core-api` | `CoursePace.java` | 신규 enum | RELAXED(3) / MODERATE(4) / PACKED(5) — 장소 수 포함 |

> enum 3개는 DB에 저장하지 않으므로 `core:core-enum`이 아닌 `core-api` web 패키지(요청 DTO 옆)에 둔다.

---

## 핵심 구현 상세

### 1. 취향 텍스트 → 즉석 임베딩 생성

```java
// AiCourseGenerationService 내부
String preferenceText = buildPreferenceText(companion, themes, pace);
// 예: "연인과 함께 감성 카페, 사진 명소 위주로 여유롭게 3곳"
float[] preferenceEmbedding = embeddingModel.embed(preferenceText);
```

### 2. AttractionMapper.xml — 새 쿼리

```sql
<select id="findCandidatesByPreferenceEmbedding">
  SELECT a.id, a.title, a.addr1, a.overview, a.content_type_id
  FROM attractions a
  JOIN attraction_embeddings ae ON ae.attraction_id = a.id
  WHERE ae.status = 'EMBEDDED'
    AND a.sido_code = #{sidoCode}
    <if test="gugunCode != null">
      AND a.gugun_code = #{gugunCode}
    </if>
  ORDER BY ae.embedding <=> #{preferenceEmbedding}::vector
  LIMIT #{limit}
</select>
```

### 3. AiCourseGenerationService — 메서드 흐름

```java
public AiCoursePreviewResponse generatePreview(Long memberId, AiCourseGenerateRequest request) {
    // Retrieval ①: 취향 임베딩 → 관광지 후보
    String preferenceText = buildPreferenceText(request);
    float[] preferenceEmbedding = embeddingClient.embed(preferenceText);
    List<AttractionEmbeddingCandidateRecord> attractions =
        attractionMapper.findCandidatesByPreferenceEmbedding(
            request.sidoCode(), request.gugunCode(), preferenceEmbedding, 20
        );

    // Retrieval ②: 유저 프로필 임베딩 → 참조 코스 (있을 때만)
    List<CourseRecord> referenceCourses = findReferenceCourses(memberId);

    // Augmentation + Generation
    AiCourseGenerationInput input = buildInput(request, attractions, referenceCourses);
    AiCourseGenerationResult result = aiCourseGenerationClient.generate(input);

    // 미리보기 응답 구성 (DB 저장 안 함)
    return buildPreviewResponse(result, attractions);
}
```

### 4. AiCourseGenerationClient — 프롬프트

**시스템 프롬프트:**
```
당신은 서울 동네 여행 코스 전문가입니다.
주어진 후보 관광지 목록에서만 장소를 선택하세요.
요청한 장소 수를 반드시 지키세요.
반드시 아래 JSON 형식으로만 응답하세요:
{"title": "...", "attractionIds": [...], "reason": "..."}
```

**사용자 프롬프트 구성:**
```
[요청 조건]
- 동네: {neighborhood}
- 동행: {companion}
- 원하는 하루: {themes}
- 속도: {pace} → {placeCount}곳

[후보 관광지 목록]
ID: 123 | 카페 OOO | 서울시 강남구 ... | 카페 | ...
...

[참고 코스] (있을 때만)
1. {코스명}: {정류장1} → {정류장2} → ...
...

[유저 취향 설명] (있을 때만)
{profile_description}
```

### 5. CoursePace enum — 장소 수 포함

```java
public enum CoursePace {
    RELAXED(3),
    MODERATE(4),
    PACKED(5);

    private final int placeCount;
}
```

---

## DB 변경

Flyway 마이그레이션 없음 — 기존 테이블 그대로 활용:
- `member_profile_embeddings` — 유저 취향 설명 + 임베딩 (있을 때만 활용)
- `attraction_embeddings` — 관광지 임베딩 (주 Retrieval 소스)
- `course_embeddings` — 코스 임베딩 (참조 코스 검색)
- `courses`, `course_items` — 저장 시 기존 흐름 그대로

---

## 구현 순서

1. `storage:db-core` — `AttractionEmbeddingCandidateRecord` + `AttractionMapper` 쿼리/메서드 추가
2. `external` — `AiCourseGenerationClient` + Input/Result DTO 구현
3. `core:core-api` — enum 3개 + 요청/응답 DTO 추가
4. `core:core-api` — `AiCourseGenerationService` 구현
5. `core:core-api` — `AiCourseController` 추가
6. 테스트 + 실제 API 검증
