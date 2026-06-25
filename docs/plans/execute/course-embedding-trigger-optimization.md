# [Plan] 코스 임베딩 트리거 최적화 — 빈 코스 및 장소 추가 시 비효율 개선

## 1. 배경 및 목적

현재 코스 생성(`createCourse`) 및 수정(`updateCourse`) 시점에 `CourseEmbeddingRequestedEvent`가 항상 발행됩니다. 
하지만 실제 사용자의 코스 작성 흐름은 다음과 같습니다.
1. **빈 껍데기 코스 생성**: 제목과 기초 정보만 가진 빈 코스를 먼저 생성합니다 (장소/정류장 개수 = 0).
2. **장소 추가/수정**: 이후 편집 화면에서 장소를 순차적으로 하나씩 추가하거나 순서를 바꿉니다.

### 기존 방식의 문제점
* **빈 코스 임베딩의 무의미함**: 장소가 없는 빈 코스는 설명(Description) 생성 및 임베딩을 수행할 정보가 부족하여 불필요한 LLM 비용과 DB 쓰기를 유발합니다.
* **장소 추가 시 반복 호출 비효율**: 장소가 추가될 때마다 매번 개별적으로 API를 호출하면, 사용자가 5개의 장소를 연속적으로 추가할 때 총 5번의 LLM/Embedding API 호출이 일어나며 비용과 레이턴시가 크게 증가합니다.

---

## 2. 장소를 추가할 때마다 임베딩을 호출하는 구조에 대한 평가

### 장점 (Pros)
* **실시간성 보장**: 코스의 변경 사항이 즉시 벡터 데이터베이스에 반영되므로, 추천 피드나 검색 결과가 최신 상태로 유지됩니다.

### 단점 (Cons)
* **API 비용 급증**: OpenAI ChatClient와 EmbeddingModel API는 토큰 단위 및 호출 횟수당 비용이 부과됩니다. 불완전한 중간 상태(장소 1개 추가됨, 2개 추가됨 등)에 대해 매번 호출하는 것은 낭비입니다.
* **성능 및 오버헤드**: `@Async`로 실행되더라도 빈번한 외부 API 호출 및 트랜잭션, DB upsert는 전체 시스템 리소스에 부담을 줍니다.
* **불완전한 설명 생성**: 장소가 1~2개뿐인 미완성 코스에 대해 생성된 LLM 요약은 최종 완성본과 비교했을 때 품질이 크게 떨어질 수 있습니다.

---

## 3. 개선 계획안

### 단기 대책 (즉시 적용 가능)
1. **정류장(Stops) 개수 검증**:
   - 코스의 정류장 개수가 최소 1개(또는 추천에 유의미한 2개 이상) 미만인 경우 이벤트를 무시하거나 발행하지 않습니다.
   - 빈 코스(`stops.isEmpty()`)인 경우 임베딩 처리를 건너뜁니다.
2. **해시 비교(이미 적용됨)**:
   - 현재 구현된 `computeSourceHash`를 통한 해시 비교 덕분에 내용 변경이 없으면 LLM을 호출하지 않지만, 장소가 추가되는 경우에는 해시가 변경되므로 임베딩이 트리거됩니다.

### 장기 대책 (추천 방향)

#### 대안 A: 상태 값(Dirty Flag) 및 스케줄러 기반 배치 처리 (배치 모드)
* **원리**: 
  - 코스가 수정될 때 즉시 외부 API를 호출하지 않고, 코스 테이블에 `embedding_status = 'PENDING'`(또는 `is_dirty = true`)와 같은 상태 값을 마킹합니다.
  - 주기적으로 작동하는 백그라운드 스케줄러(예: 5분~10분 간격)가 `PENDING` 상태이면서 수정된 지 일정 시간(예: 3분)이 지난 코스들을 수집하여 일괄 임베딩 처리합니다.
* **효과**: 사용자가 집중적으로 코스를 편집하는 동안(Write burst) 발생하는 임베딩 요청을 최종 1회로 병합(Debouncing)하여 API 호출 횟수를 효율적으로 줄일 수 있습니다.

#### 대안 B: 명시적 발행/완료(Publish) 액션 도입
* **원리**: 
  - 사용자가 편집을 완전히 마치고 "저장 완료" 또는 "발행(Publish)" 버튼을 누를 때만 서버가 임베딩 이벤트를 발행하도록 프론트엔드와 API 계약을 수정합니다.
* **효과**: 비즈니스 흐름상 가장 명확하며 불필요한 연산을 원천적으로 차단합니다.

---

## 4. 구체적인 수정 계획 (단기 대책 중심)

### Phase 1 — 빈 코스 및 최소 조건 검증 추가
* **[CourseEmbeddingEventListener](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/event/listener/CourseEmbeddingEventListener.java)** 수정:
  ```java
  // handleCourseEmbeddingRequested 내 추가
  if (record == null) {
      return;
  }
  
  // 정류장이 없거나 최소 기준에 미달하는 경우 건너뜀
  if (record.getStopTitles() == null || record.getStopTitles().isBlank()) {
      log.debug("코스 임베딩 건너뜀 - 정류장 정보 없음, courseId: {}", courseId);
      return;
  }
  ```

### Phase 2 — 빈 껍데기 코스 생성 시 이벤트 발행 생략
* **[CourseWriter](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/CourseWriter.java)** 수정:
  ```java
  @Transactional
  public Course create(Course course) {
      ...
      // 생성 시점에 stops가 비어 있다면 이벤트 발행 생략
      if (!plannedStops.isEmpty()) {
          eventPublisher.publishEvent(new CourseEmbeddingRequestedEvent(course.id()));
      }
      return created;
  }
  ```

---

## 5. 결론 및 피드백 제안
1. **우선 순위**: 빈 코스일 때 임베딩을 건너뛰는 예외 검증 로직([Phase 1, 2](#4-구체적인-수정-계획-단기-대책-중심))을 먼저 도입합니다.
2. **장소 추가 주기 최적화**: 
   - 실시간 추천 반영이 핵심 요구사항이라면 매 장소 추가 시 트리거하되 최소 2개 이상 장소가 있을 때만 API를 호출하도록 제한하는 것이 가성비 면에서 훌륭합니다.
   - 트래픽이 커질 예정이라면 **대안 A(Dirty Flag + 스케줄러 배치)** 방식을 적극 추천합니다.
