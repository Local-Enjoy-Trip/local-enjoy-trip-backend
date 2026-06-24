# 코스(Course) 도메인 단순화 및 격벽 재설계 계획

이 계획서는 코스 도메인의 기술적 세분화(`CourseRoute`, `CourseRouteSegment`, `RouteSummary`,
`course_route_segments`)를 제거하고, 제품 개념을 **코스(Course)**와 **경유지(CourseStop)** 중심으로
재정렬하기 위한 실행 계획이다.

핵심 결정은 다음과 같다.

- 도메인 모델에서는 `CourseRoute`, `CourseRouteSegment`, `RouteSummary`를 삭제한다.
- 이동 거리/시간은 별도 segment가 아니라 **현재 경유지에서 다음 경유지까지의 상태**로 `CourseStop`에 둔다.
- DB에서는 `course_route_segments` 테이블을 제거하고, 기존 값을 `course_items.distance_to_next`,
  `course_items.duration_to_next`로 이관한다.
- `travelMode`는 저장/API/도메인에서 제거한다.
- API 응답은 `segments` 노출을 중단하고, `routeSummary`는 별도 도메인 타입 없이 `Course`/`CourseStop`
  상태에서 파생한다.

---

## 1. 목표와 비목표

### 목표

1. **도메인 개념 축소**
   - 남는 핵심 개념은 `Course`, `CourseStop`, `CourseStopTarget`이다.
   - `CourseStopTarget`은 관광지/노트 대상 식별을 위한 값 객체로 유지한다.

2. **이동 정보의 소유권 단순화**
   - `distanceToNext`, `durationToNext`를 `CourseStop` 상태로 둔다.
   - 전체 거리/시간은 `Course`가 stops를 합산해 계산한다.

3. **저장소 구조 단순화**
   - `course_route_segments`를 제거한다.
   - `course_items`가 경유지 자체와 다음 경유지까지의 이동 상태를 함께 소유한다.

4. **API 계약 정리**
   - `CourseResponse.segments`를 제거한다.
   - `CourseResponse.routeSummary`는 유지하되 도메인 `RouteSummary` 타입 없이 응답 DTO에서 파생한다.
   - `CourseItemResponse`에 `distanceToNext`, `durationToNext`를 포함한다.

5. **AI 추천/좌표 기반 경로 계산 흐름 유지**
   - 추천 기능은 계속 preview-only로 동작한다.
   - 추천 결과는 재정렬된 stops와 각 stop의 next metric을 가진 `Course`로 반환한다.
   - 저장은 기존 `PUT /api/courses/{id}` 경로를 사용한다.

### 비목표

- 코스 생성/수정 API의 요청 모양을 대규모로 바꾸지 않는다.
- 관리자 큐레이션, 저장 수, 거리순 피드, 시작 좌표(`startLocation`) 기능을 제거하지 않는다.
- `core-api -> storage:db-core` 직접 MyBatis 사용 구조를 바꾸지 않는다.
- `external` 모듈에 core/domain 타입을 의존시키지 않는다.

---

## 2. 최종 도메인 모델

### 2.1 Course

`Course`는 코스 aggregate root다. 현재 API/피드/관리자 기능이 사용하는 상태는 유지하고, route wrapper만 제거한다.

#### 유지 상태

- `id`
- `ownerMemberId`
- `title`
- `regionName`
- `visibility`
- `status`
- `description`
- `coverImageUrl`
- `curationSection`
- `curationOrder`
- `createdByAdmin`
- `startLatitude`
- `startLongitude`
- `distanceMeters`
- `saveCount`
- `createdAt`
- `updatedAt`
- `stops: List<CourseStop>`

#### 행위

- `requireOwnedBy(Long memberId)`
- `items()` 또는 `stops()`
  - 기존 호출부 전환 비용을 줄이려면 `items()`를 호환 메서드로 잠시 유지할 수 있다.
  - 새 코드에서는 `stops()`를 기준 이름으로 사용한다.
- `withStops(List<CourseStop> stops)`
- `withStartLocation(CourseStopPoint startPoint)`
- `stopCount()`
- `segmentCount()`
  - `max(stops.size() - 1, 0)`로 계산한다.
- `totalDistanceMeters()`
  - `distanceToNext == null`은 0으로 합산한다.
- `totalDurationSeconds()`
  - `durationToNext == null`은 0으로 합산한다.

#### 제거

- `CourseRoute route`
- `route()`
- `withRoute(CourseRoute)`
- `routeSummary()`
  - 도메인 `RouteSummary`를 반환하지 않는다. 필요한 값은 위 파생 행위로 제공한다.

### 2.2 CourseStop

`CourseStop`은 코스 내부의 경유지 child model이다. 저장소 식별자(`id`)를 가지므로 문서에서는
“Value Object”라고 부르지 않고 **Course 내부 경유지 모델**로 부른다.

#### 상태

- `id`
- `target: CourseStopTarget`
- `position`
- `day`
- `memo`
- `stayMinutes`
- `title`
- `distanceToNext`
- `durationToNext`

#### 검증

- `target != null`
- `position > 0`
- `day > 0`
- `stayMinutes == null || stayMinutes > 0`
- `distanceToNext == null || distanceToNext >= 0`
- `durationToNext == null || durationToNext >= 0`

#### 행위

- `withId(Long nextId)`
- `withPosition(int nextPosition)`
- `withTitle(String nextTitle)`
- `withNextMetrics(Integer distanceToNext, Integer durationToNext)`
- `withoutStorageId()`

### 2.3 삭제 대상 도메인 타입

- `core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/CourseRoute.java`
- `core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/CourseRouteSegment.java`
- `core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/RouteSummary.java`

---

## 3. API 계약 변경

### 3.1 CourseResponse

#### 변경 전

- `routeSummary`
- `items`
- `segments`

#### 변경 후

- `routeSummary`
  - 유지한다.
  - `RouteSummaryResponse.from(Course)` 또는 `RouteSummaryResponse.of(...)`로 생성한다.
  - domain `RouteSummary` 타입에 의존하지 않는다.
- `items`
  - 각 item에 `distanceToNext`, `durationToNext`를 추가한다.
- `segments`
  - 제거한다.

### 3.2 CourseItemResponse

추가 필드:

- `distanceToNext`
- `durationToNext`

### 3.3 RouteSummaryResponse

domain `RouteSummary` import를 제거하고 다음 값으로 구성한다.

- `stopCount = course.stopCount()`
- `segmentCount = course.segmentCount()`
- `totalDurationSeconds = course.totalDurationSeconds()`
- `totalDistanceMeters = course.totalDistanceMeters()`

### 3.4 CourseSegmentResponse

삭제한다. `CourseResponse.segments`가 제거되면 사용처가 없다.

### 3.5 API 문서

`docs/api/courses.md`를 갱신한다.

- 생성/수정 응답 설명에서 `segments` 제거
- `routeSummary`가 item의 next metric 합산 결과임을 명시
- 추천 preview 응답도 동일한 `CourseResponse` 계약을 사용한다고 명시
- 기존 “route segment rows” 표현을 “item next metrics”로 변경

---

## 4. DB 및 storage:db-core 변경

### 4.1 최종 DB 결정

`course_route_segments` 테이블은 제거한다. 이동 metric은 `course_items`에 흡수한다.

### 4.2 Migration

새 Flyway migration을 추가한다.

1. `course_items` 컬럼 추가
   - `distance_to_next integer`
   - `duration_to_next integer`

2. 제약 추가
   - `distance_to_next is null or distance_to_next >= 0`
   - `duration_to_next is null or duration_to_next >= 0`

3. 기존 데이터 이관
   - `course_route_segments.from_course_item_id = course_items.id` 기준으로 update
   - `distance_meters -> course_items.distance_to_next`
   - `duration_seconds -> course_items.duration_to_next`
   - 마지막 경유지는 next metric이 없으므로 null 유지

4. 기존 segment 테이블 제거
   - `drop table course_route_segments`

5. 기존 migration 파일은 수정하지 않는다.
   - 이미 적용된 Flyway migration을 바꾸지 않고 새 migration으로 전진한다.

### 4.3 CourseItemRecord / CourseItemDetailRecord

필드 추가:

- `distanceToNext`
- `durationToNext`

Lombok 생성자/게터 사용부와 MyBatis result alias를 함께 갱신한다.

### 4.4 CourseRouteSegmentRecord

삭제한다.

- `storage/db-core/src/main/java/com/ssafy/enjoytrip/storage/db/core/model/CourseRouteSegmentRecord.java`

### 4.5 CourseMapper.java

제거:

- `insertSegment`
- `insertSegments`
- `findSegmentsByCourseId`
- `deleteSegmentsByCourseId`

유지/변경:

- `insertItem`
- `insertItems`
  - `distance_to_next`, `duration_to_next` insert 포함
- `findItemsByCourseId`
  - next metric column select 포함
- `findPublicItemsByCourseId`
  - next metric column select 포함
- `deleteItemsByCourseId`

### 4.6 CourseMapper.xml

제거:

- `courseRouteSegmentColumns`
- `deleteSegmentsByCourseId`
- `insertSegment`
- `insertSegments`
- `findSegmentsByCourseId`

변경:

- `courseItemColumns`에 next metric 추가
- `insertItem`, `insertItems`에 next metric 추가
- item select 쿼리는 기존 order 유지

### 4.7 H2 test schema/support

변경:

- `storage/db-core/src/test/resources/h2/mapper-schema.sql`
  - `course_items`에 next metric 컬럼/제약 추가
  - `course_route_segments` table drop/create 제거
- `H2MapperTestSupport`
  - `delete from course_route_segments` 제거
- mapper H2 테스트
  - segment insert/select/constraint 테스트 제거
  - item next metric 저장/조회 테스트 추가

---

## 5. core-api 변경

### 5.1 요청 DTO

대상:

- `CourseCreateRequest`
- `CourseUpdateRequest`
- `AdminCourseForm`

변경:

- `CourseRoute.ofStops(...)` 생성 제거
- `List<CourseStop>` 또는 `Course` 생성자에 직접 stops 전달
- 요청에서 next metric을 받을지 여부를 명확히 한다.
  - 1차 리팩토링에서는 클라이언트 요청으로 `distanceToNext`, `durationToNext`를 받지 않는다.
  - 생성/수정 시 서버가 좌표 기반으로 계산한다.
  - 향후 클라이언트가 직접 metric을 보낼 필요가 생기면 별도 API 계약 변경으로 다룬다.

### 5.2 CourseReader

변경 전:

- course record 조회
- item 조회
- segment 조회
- item + segment 조립해 `CourseRoute` 생성

변경 후:

- course record 조회
- item 조회
- item record의 next metric을 포함해 `CourseStop` 생성
- `new Course(..., stops)` 직접 생성

제거:

- `SegmentReadPolicy`
- `findSegmentsByCourseId` 호출
- `CourseRouteSegmentRecord` 변환
- incomplete segment fallback 로직

### 5.3 CourseWriter

변경 전:

- `CourseRoute`를 계획한다.
- stop rows 저장 후 segment rows 저장한다.

변경 후:

- 입력 stops를 `CourseStopPointResolver`로 좌표 조회한다.
- `CourseRoutePlanner`가 stops에 next metric을 채운다.
- item rows만 저장한다.
- update 시 기존처럼 `course_items`를 delete 후 재insert한다.
- segment delete/insert는 없다.

주의:

- `updateStartLocation`은 유지한다.
- start location은 첫 point 기준으로 유지한다.
- 좌표가 부족한 경우 기존 실패 정책을 유지할지, metric null 허용으로 바꿀지 결정해야 한다.
  - 권장: 기존 정책 유지. 좌표 기반 planning이 필요한 create/update/recommendation에서는 좌표 부족을 `COURSE_INVALID_ITEM`으로 처리한다.
  - 단, 기존 DB에서 next metric이 null인 조회는 허용한다.

### 5.4 CourseRoutePlanner

현재:

```java
CourseRoute plan(List<CourseStopPoint> points);
```

변경:

```java
List<CourseStop> plan(List<CourseStopPoint> points);
```

동작:

- 입력 순서대로 stop position을 1부터 재정규화한다.
- 각 stop에 다음 stop까지의 `distanceToNext`, `durationToNext`를 채운다.
- 마지막 stop의 next metric은 null로 둔다.
- 0개 또는 1개 stop은 metric 없이 반환한다.

### 5.5 DefaultCourseRoutePlanner

변경:

- `CourseRouteSegment` 생성 제거
- `CourseStop.withNextMetrics(...)` 사용
- `DEFAULT_TRAVEL_MODE` 제거
- 거리/시간 계산 로직은 유지

### 5.6 AiCourseOrderOptimizer

변경:

- `planPreviewRoute(...)` 반환 타입을 `List<CourseStop>` 또는 `Course` 조립에 맞게 변경
- `requirePlannedSegments(...)`, `hasCompleteSegmentSet(...)` 제거
- “완전한 metric set” 검증은 stops 기준으로 변경
  - stops size가 2 이상이면 마지막을 제외한 모든 stop의 next metric이 non-null인지 확인
- 추천 실패 fallback은 기존 정책 유지
  - AI 실패 시 좌표 fallback
  - 좌표 fallback도 불가하면 현재 저장 순서 반환

### 5.7 CourseOrderPreviewReader / CourseStopPointResolver

변경:

- `CourseRoute` 의존 제거
- `List<CourseStop>` 기반으로 동작
- preview item title 주입 후 planner로 넘기는 흐름 유지

---

## 6. 테스트 변경 계획

### 6.1 삭제/대체

삭제 또는 대체:

- `CourseRouteTest`
  - `CourseTest`, `CourseStopTest`로 대체
- `CourseRouteSegment` 관련 테스트
- `CourseSegmentResponse` 문서화 테스트

### 6.2 추가/수정 테스트

#### domain

- `Course`가 total distance/duration을 stops에서 합산한다.
- null next metric은 합산에서 0으로 처리한다.
- `CourseStop`은 음수 next metric을 거부한다.
- `DefaultCourseRoutePlanner`는 마지막 stop metric을 null로 둔다.
- `DefaultCourseRoutePlanner`는 입력 순서대로 position을 재정규화한다.

#### service

- create 시 item rows에 next metric이 저장된다.
- update 시 기존 item rows를 교체하고 next metric도 새 순서 기준으로 저장된다.
- recommendation preview는 저장하지 않고 next metric이 채워진 응답을 반환한다.
- 좌표 부족 fallback/실패 정책이 기존 제품 정책과 동일하게 동작한다.

#### web

- `CourseResponse`에 `segments`가 없다.
- `items[*].distanceToNext`, `items[*].durationToNext`가 있다.
- `routeSummary` 값이 item next metric 합산과 일치한다.

#### storage

- `insertItems`가 next metric을 저장한다.
- `findItemsByCourseId`가 next metric을 조회한다.
- H2 schema 제약이 음수 next metric을 거부한다.
- segment mapper 메서드가 더 이상 존재하지 않는다.

---

## 7. 실행 순서

### Phase 1. Storage foundation

1. Flyway migration 추가
   - `course_items` next metric 컬럼 추가
   - 기존 segment 데이터 이관
   - `course_route_segments` drop

2. H2 schema 동기화

3. storage model 갱신
   - `CourseItemRecord`
   - `CourseItemDetailRecord`

4. mapper interface/xml 갱신
   - segment API 제거
   - item insert/select next metric 반영

5. storage 테스트 갱신 및 실행
   - `./gradlew :storage:db-core:test`

### Phase 2. Domain model 전환

1. `CourseStop`에 next metric 추가
2. `Course`를 `List<CourseStop>` 직접 보유 구조로 변경
3. `CourseRoute`, `CourseRouteSegment`, `RouteSummary` 삭제
4. `CourseRoutePlanner` 반환 타입 변경
5. `DefaultCourseRoutePlanner`를 stop metric planner로 변경
6. domain 테스트 갱신 및 실행
   - `./gradlew :core:core-api:test --tests '*Course*' --tests '*RoutePlanner*'`

### Phase 3. Reader/Writer/Application 전환

1. `CourseReader`에서 segment read policy 제거
2. `CourseWriter`에서 segment delete/insert 제거
3. `CourseService`, `AdminCourseService` 호출부 확인
4. `AiCourseOrderOptimizer`, preview reader/resolver 흐름 갱신
5. service 테스트 갱신 및 실행
   - `./gradlew :core:core-api:test --tests '*CourseServiceTest' --tests '*AdminCourseServiceTest'`

### Phase 4. API contract 정리

1. `CourseResponse`에서 `segments` 제거
2. `CourseItemResponse`에 next metric 추가
3. `RouteSummaryResponse`를 `Course` 파생값 기반으로 변경
4. `CourseSegmentResponse` 삭제
5. REST Docs/API 문서 갱신
6. web/doc 테스트 갱신 및 실행
   - `./gradlew :core:core-api:test --tests '*CourseControllerTest' --tests '*ApiDocumentationTest'`

### Phase 5. 통합 검증

1. 컴파일/테스트
   - `./gradlew :core:core-api:check`
   - `./gradlew :storage:db-core:check`

2. 검색 검증
   - `CourseRoute`, `CourseRouteSegment`, `RouteSummary`, `course_route_segments`, `travelMode` 잔여 참조 확인
   - 남은 참조가 migration 이력 파일처럼 의도된 경우만 허용

3. 문서 검증
   - `docs/api/courses.md`
   - 이 계획서

---

## 8. 위험과 대응

### API breaking change

- `segments` 제거는 클라이언트 breaking change다.
- 프론트가 아직 `segments`를 사용하면 한 릴리스 동안 deprecated 파생 `segments` 응답을 유지하는 선택지가 있다.
- 기본 계획은 제거지만, 배포 조율이 필요하면 Phase 4에서 호환 유지 여부를 결정한다.

### 기존 데이터 이관

- 기존 `course_route_segments`가 일부 누락된 코스는 next metric이 null로 남는다.
- 조회/summary는 null을 0으로 처리한다.
- 새 create/update/recommendation 결과는 가능한 한 metric을 채운다.

### 좌표 부족

- 현재 경로 계산은 좌표가 없으면 실패한다.
- 이 정책을 완화하면 create/update semantics가 바뀌므로 이 계획에서는 기존 실패 정책을 유지한다.
- 기존 저장 데이터 조회에서만 null metric을 허용한다.

### migration rollback

수동 rollback이 필요하면 다음 방향으로 작성한다.

- `course_route_segments` 재생성
- `course_items.distance_to_next/duration_to_next` 기준으로 인접 item segment 재구성
- `course_items` next metric 컬럼 drop

실제 rollback SQL은 migration 작성 시 작업 메모에 남긴다.

---

## 9. 완료 조건

- product source에 `CourseRoute`, `CourseRouteSegment`, `RouteSummary` 타입이 남아 있지 않다.
- `storage:db-core` runtime mapper에 `course_route_segments` 접근이 남아 있지 않다.
- `travelMode`가 코스 도메인/API/storage runtime 경로에서 제거되어 있다.
- `CourseResponse`는 `segments` 없이 `items`와 `routeSummary`로 이동 정보를 표현한다.
- `:core:core-api:check`, `:storage:db-core:check`가 통과한다.
- 완료 보고에는 사용한 규칙 계층을 명시한다.
  - `CONSTITUTION.md`
  - `RULES.md`
  - `docs/code-review.md`