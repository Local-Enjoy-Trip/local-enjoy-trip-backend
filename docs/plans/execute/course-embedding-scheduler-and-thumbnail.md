# [Plan] 코스 임베딩 스케줄러 전환 + 코스 대표 사진

## 목표

1. 코스 생성/수정 시 즉시 LLM을 호출하는 이벤트 기반 방식을 **스케줄러 기반 배치**로 교체
2. 경유지 2개 미만인 코스는 임베딩 대상에서 제외
3. `course_embeddings.status`의 `PENDING` 상태를 Dirty Flag로 활용
4. 스케줄러 조회와 상태 변경을 원자적 쿼리 하나로 묶어 중복 처리 방지
5. `courses` 테이블에 `thumbnail_url` 추가 — 첫 번째 경유지(attraction.first_image 또는 note.image_url)

---

## 현재 구조 요약

```
CourseWriter.create/update
  → publishEvent(CourseEmbeddingRequestedEvent)
    → CourseEmbeddingEventListener.handleCourseEmbeddingRequested (Async)
      → hash 비교 → LLM 호출 → upsertEmbedded/upsertFailed
```

**제거 대상**: `CourseEmbeddingRequestedEvent`, `CourseEmbeddingEventListener`

**신규 구조**:

```
CourseWriter.create/update
  → stops >= 2이면 markPending(courseId)   (course_embeddings upsert, status='PENDING')
  → updateThumbnailUrl(courseId)            (첫 경유지 이미지를 courses에 반영)

CourseEmbeddingScheduler (@Scheduled 5분)
  → claimPendingBatch(limit)               (PENDING → PROCESSING, RETURNING course_id)
  → 각 courseId: hash 비교 → LLM → upsertEmbedded/upsertFailed
```

---

## Phase 1 — DB Migration (V23)

**파일**: `storage/db-core/src/main/resources/db/migration/V23__course_embedding_scheduler_and_thumbnail.sql`

```sql
-- 1. course_embeddings: PROCESSING 상태 추가
ALTER TABLE course_embeddings
    DROP CONSTRAINT chk_course_embeddings_status;
ALTER TABLE course_embeddings
    ADD CONSTRAINT chk_course_embeddings_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'EMBEDDED', 'FAILED'));

-- 2. courses: 대표 사진 컬럼 추가
ALTER TABLE courses
    ADD COLUMN thumbnail_url VARCHAR(1024);
```

---

## Phase 2 — CourseEmbeddingMapper

**인터페이스** (`storage/db-core/CourseEmbeddingMapper.java`):
- `markPending(courseId)` 추가
- `claimPendingBatch(limit)` 추가 — `List<String>` 반환
- `findSourceHashByCourseId` 유지 (스케줄러에서 hash 비교에 재사용)
- `upsertEmbedded`, `upsertFailed` 유지

**XML** (`CourseEmbeddingMapper.xml`):

```xml
<!-- markPending: stops >= 2인 경우 CourseWriter가 직접 호출 -->
<!-- PROCESSING 중인 코스는 덮어쓰지 않음 (이미 처리 중인 항목 보호) -->
<insert id="markPending">
    insert into course_embeddings (
        course_id, source_version, source_hash,
        embedding_dimension, provider, model,
        status, attempt_count, updated_at
    )
    values (
        #{courseId}, 'v1', '',
        1536, 'openai', 'unknown',
        'PENDING', 0, current_timestamp
    )
    on conflict (course_id) do update set
        status     = case when course_embeddings.status = 'PROCESSING'
                         then 'PROCESSING'
                         else 'PENDING' end,
        updated_at = current_timestamp
</insert>

<!-- claimPendingBatch: PENDING 코스를 원자적으로 PROCESSING으로 전환 -->
<!-- FOR UPDATE SKIP LOCKED: 다른 트랜잭션이 처리 중인 행은 건너뜀 -->
<select id="claimPendingBatch" resultType="string">
    with claimed as (
        select ce.course_id
        from course_embeddings ce
        join courses c on c.id = ce.course_id and c.deleted_at is null
        where ce.status = 'PENDING'
          and (
              select count(*)
              from course_items
              where course_id = ce.course_id
          ) >= 2
        order by ce.updated_at asc
        limit #{limit}
        for update skip locked
    )
    update course_embeddings
    set status     = 'PROCESSING',
        updated_at = current_timestamp
    where course_id in (select course_id from claimed)
    returning course_id
</select>
```

---

## Phase 3 — CourseMapper (thumbnail_url)

**XML** (`CourseMapper.xml`):

```xml
<update id="updateThumbnailUrl">
    update courses
    set thumbnail_url = (
        select case ci.item_type
                   when 'ATTRACTION' then a.first_image
                   when 'NOTE'       then n.image_url
               end
        from course_items ci
        left join attractions a on a.id = ci.attraction_id
        left join notes n       on n.id = ci.note_id
        where ci.course_id = #{courseId}
          and (
              (ci.item_type = 'ATTRACTION' and a.first_image  is not null and a.first_image  &lt;&gt; '')
              or
              (ci.item_type = 'NOTE'       and n.image_url    is not null and n.image_url    &lt;&gt; '')
          )
        order by ci.position asc, ci.id asc
        limit 1
    ),
    updated_at = current_timestamp
    where id = #{courseId}
      and deleted_at is null
</update>
```

**인터페이스** (`CourseMapper.java`): `updateThumbnailUrl(courseId)` 추가

---

## Phase 4 — CourseWriter 변경

변경 내용:
- `ApplicationEventPublisher` 의존성 제거
- `CourseEmbeddingMapper` 의존성 추가
- `create()`, `update()`: 이벤트 발행 → `markPending` + `updateThumbnailUrl` 교체

```java
@Transactional
public Course create(Course course) {
    // ... 기존 로직 동일 ...
    Course created = course.withStartLocation(startPoint)
            .withStops(saveStops(course.id(), plannedStops));

    courseMapper.updateThumbnailUrl(course.id());
    if (plannedStops.size() >= 2) {
        courseEmbeddingMapper.markPending(course.id());
    }
    return created;
}

@Transactional
public Course update(Course course) {
    // ... 기존 로직 동일 ...
    Course updated = course.withStartLocation(startPoint)
            .withStops(saveStops(course.id(), plannedStops));

    courseMapper.updateThumbnailUrl(course.id());
    if (plannedStops.size() >= 2) {
        courseEmbeddingMapper.markPending(course.id());
    }
    return updated;
}
```

---

## Phase 5 — CourseEmbeddingScheduler (신규)

**위치**: `core/core-api/src/main/java/com/ssafy/enjoytrip/core/api/worker/course/CourseEmbeddingScheduler.java`

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseEmbeddingScheduler {

    private static final String SOURCE_VERSION = "v1";
    private static final int BATCH_LIMIT = 20;

    private final CourseEmbeddingMapper courseEmbeddingMapper;
    private final CourseEmbeddingClient courseEmbeddingClient;

    @Scheduled(fixedDelayString = "${enjoytrip.course.embedding.flush-delay-ms:300000}")
    public void embedPendingCourses() {
        List<String> courseIds = courseEmbeddingMapper.claimPendingBatch(BATCH_LIMIT);
        if (courseIds.isEmpty()) {
            return;
        }
        log.info("코스 임베딩 배치 시작 - count={}", courseIds.size());
        for (String courseId : courseIds) {
            embedOne(courseId);
        }
    }

    private void embedOne(String courseId) {
        CourseEmbeddingInputRecord record =
                courseEmbeddingMapper.findCourseEmbeddingInputById(courseId);
        if (record == null) {
            log.debug("코스 임베딩 건너뜀 - 코스 없음, courseId={}", courseId);
            return;
        }

        CourseEmbeddingInput input = new CourseEmbeddingInput(
                record.getCourseId(), record.getTitle(),
                record.getRegionName(), record.getTagNames(), record.getStopTitles()
        );
        String sourceHash = computeSourceHash(input);
        String existingHash = courseEmbeddingMapper.findSourceHashByCourseId(courseId);
        if (Objects.equals(existingHash, sourceHash)) {
            // 내용 변화 없음 → 바로 EMBEDDED로 복귀
            courseEmbeddingMapper.upsertEmbedded(courseId, null, null,
                    record.getDominantCategory(), SOURCE_VERSION, sourceHash,
                    1536, "openai", "unknown");
            log.debug("코스 임베딩 건너뜀 - 변경 없음, courseId={}", courseId);
            return;
        }

        try {
            CourseEmbeddingResult result = courseEmbeddingClient.embed(input);
            courseEmbeddingMapper.upsertEmbedded(
                    courseId, result.description(), toVectorLiteral(result.embedding()),
                    record.getDominantCategory(), SOURCE_VERSION, sourceHash,
                    result.dimension(), result.provider(), result.model()
            );
            log.info("코스 임베딩 완료 - courseId={}", courseId);
        } catch (CourseEmbeddingException ex) {
            log.error("코스 임베딩 실패 - courseId={}, code={}", courseId, ex.failureCode(), ex);
            courseEmbeddingMapper.upsertFailed(courseId, SOURCE_VERSION, sourceHash,
                    "openai", "unknown", ex.failureCode(), limitMessage(ex.getMessage()));
        } catch (RuntimeException ex) {
            log.error("코스 임베딩 예기치 않은 실패 - courseId={}", courseId, ex);
            courseEmbeddingMapper.upsertFailed(courseId, SOURCE_VERSION, sourceHash,
                    "openai", "unknown", "COURSE_EMBEDDING_ERROR", limitMessage(ex.getMessage()));
        }
    }
    // computeSourceHash, toVectorLiteral, limitMessage — CourseEmbeddingEventListener에서 이관
}
```

---

## Phase 6 — 제거 대상

- `CourseEmbeddingRequestedEvent.java` 삭제
- `CourseEmbeddingEventListener.java` 삭제

---

## Phase 7 — application.yml 프로퍼티 추가

```yaml
enjoytrip:
  course:
    embedding:
      flush-delay-ms: 300000   # 5분
      batch-limit: 20
```

---

## 변경 파일 목록

| 작업 | 파일 |
|---|---|
| 신규 | `storage/.../db/migration/V23__course_embedding_scheduler_and_thumbnail.sql` |
| 수정 | `storage/.../mapper/CourseEmbeddingMapper.java` |
| 수정 | `storage/.../mapper/CourseEmbeddingMapper.xml` |
| 수정 | `storage/.../mapper/CourseMapper.java` |
| 수정 | `storage/.../mapper/CourseMapper.xml` |
| 수정 | `core/.../domain/CourseWriter.java` |
| 신규 | `core/.../api/worker/course/CourseEmbeddingScheduler.java` |
| 삭제 | `core/.../domain/event/CourseEmbeddingRequestedEvent.java` |
| 삭제 | `core/.../domain/event/listener/CourseEmbeddingEventListener.java` |
| 수정 | `core/.../resources/application.yml` |

---

## 체크리스트

- [ ] Phase 1: V23 마이그레이션 작성 및 적용
- [ ] Phase 2: `CourseEmbeddingMapper` 인터페이스 + XML (`markPending`, `claimPendingBatch`)
- [ ] Phase 3: `CourseMapper` 인터페이스 + XML (`updateThumbnailUrl`)
- [ ] Phase 4: `CourseWriter` — 이벤트 제거, `markPending` + `updateThumbnailUrl` 추가
- [ ] Phase 5: `CourseEmbeddingScheduler` 신규 작성
- [ ] Phase 6: `CourseEmbeddingRequestedEvent`, `CourseEmbeddingEventListener` 삭제
- [ ] Phase 7: application.yml 프로퍼티 추가
- [ ] 검증: `:core:core-api:check`, `:storage:db-core:check`
