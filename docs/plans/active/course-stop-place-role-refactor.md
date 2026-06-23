# 코스 경유지 역할 모델 개선 계획

## 1. 목표

코스 생성/수정 흐름에서 관광지와 쪽지를 매번 별도 분기하지 않도록, 코스 도메인 안에서는 둘을
**코스에 배치 가능한 지점**이라는 역할로 통일한다.

핵심 목표는 다음과 같다.

- 장소와 쪽지를 전역적으로 같은 엔티티로 합치지 않는다.
- 코스 안에서만 공통 역할을 둔다.
- 공개 여부, 존재 여부, 좌표, 제목 조회는 한 경계에서 resolve한다.
- route planning, segment 계산, response 조립은 resolved stop point를 기준으로 동작하게 한다.
- 기존 public API의 `items[].itemType`, `attractionId`, `noteId` 계약은 유지한다.
- DB의 `course_items.item_type + attraction_id + note_id` XOR 구조는 storage contract로 유지한다.

## 2. 현재 문제

현재 도메인에는 이미 `CourseStopTarget`이 있어 attraction/note target을 감싸고 있다. 하지만 target이
코스에 필요한 공통 속성까지 제공하지 못하므로 아래 분기가 여러 경계에 남아 있다.

- web request: `CourseItemRequest`가 `itemType`, `attractionId`, `noteId` 조합을 해석한다.
- service planning: `CourseService.toCandidate(...)`가 `Attraction`과 `Note`를 나눠 좌표를 조회한다.
- storage write: `CourseService.toItemRecord(...)`가 target별 nullable FK를 다시 만든다.
- storage read: `CourseService.target(...)`, `itemTitle(...)`가 mapper projection을 다시 분기한다.
- SQL read: `CourseMapper.findPublicItemsByCourseId(...)`가 `ATTRACTION`과 `NOTE` 공개 조건을 각각 가진다.

이 구조에서는 코스가 필요한 것은 "방문 가능한 지점의 id, type, title, 좌표, 공개 가능성"인데, 구현은
"관광지일 때 / 쪽지일 때" 조건문으로 계속 흩어진다.

## 3. 설계 방향

### 3.1 도메인 언어

이 계획에서 사용할 이름은 다음과 같다.

- `CourseStopTarget`: 저장/API 계약의 target identity. `ATTRACTION(id)` 또는 `NOTE(id)`를 표현한다.
- `CourseStop`: 코스 route 안의 stop. target identity와 stop metadata를 가진다.
- `CourseStopPoint`: 코스에 실제 배치 가능한 resolved point. 원래 stop과 planning에 필요한 공통 속성을 가진다.
- `CourseRoute`: 순서가 정규화된 stop 목록과 segment 목록을 가진다.
- `CourseRoutePlanner`: route planning 전략 경계. 현재 내부 알고리즘과 미래 AI 추천 경로 구현체를 같은 계약 뒤에 둔다.

`CourseStopTarget`은 "무엇을 가리키는가"만 표현한다.
`CourseStopPoint`는 "코스에 놓을 수 있는가, 그리고 경로 계산에 필요한 값은 무엇인가"를 표현한다.
`CourseRoutePlanner`는 `Course` 엔티티 내부 행위가 아니라 course use case의 전략 collaborator다.
`Course`/`CourseRoute`는 planner를 호출하지 않고, planner가 반환한 route의 정합성을 검증한다.

예상 형태:

```java
public record CourseStopPoint(
        CourseStop stop,
        String title,
        Double latitude,
        Double longitude
) {
}
```

이 타입은 persistence row mirror가 아니라 코스 use case의 resolved read model이다.

### 3.2 Resolve 경계

관광지/쪽지 분기는 `CourseStopPointResolver` 같은 좁은 collaborator 하나로 모은다.

역할:

- 입력: `List<CourseStop>`
- 출력: `List<CourseStopPoint>`
- 책임:
  - target type별 존재 여부 확인
  - public/active 상태 확인
  - title/latitude/longitude 조회
  - 원래 stop metadata와 resolved point를 결합
  - 가능하면 target type별 distinct id를 묶어 batch 조회한다.

금지:

- controller에서 mapper 호출
- storage Record에 core domain 변환 메서드 추가
- `core-api`가 `external`용 port/gateway를 새로 만드는 방식의 과잉 추상화
- 장소/쪽지 저장소 모델 자체를 하나로 합치는 schema 변경
- resolver가 `CourseRoutePlanner.StopCandidate` 같은 planner 내부 DTO를 생성하는 것

초기 구현은 `core-api` 안의 non-service collaborator로 둔다. 예를 들어:

```text
core.domain.service.CourseStopPointResolver
```

이름은 service-to-service 규칙을 피하기 위해 `*Service`보다 좁은 책임 이름을 우선한다.
초기 mapper가 note batch 조회를 지원하지 않으면 stop 단위 조회를 임시로 허용하되, public API는 `resolveAll(...)`로 둔다.

### 3.3 Route planning 전략 경계

`CourseRoutePlanner`는 course bounded context 안의 전략 계약으로 유지한다.
planner 구현체를 `Course` 또는 `CourseRoute` 안으로 넣지 않는다.

역할 분리:

- `CourseStopPointResolver`: 저장소를 읽어 stop이 코스에 배치 가능한지 resolve한다.
- `CourseRoutePlanner`: resolved point 목록으로 최종 stop 순서와 segment를 만든다.
- `CourseRoute`: planner 결과가 코스 route로 유효한지 검증한다.
- `CourseService`/`AdminCourseService`: resolve, plan, persistence write를 orchestration한다.

구현 방향:

- `CourseRoutePlanner`를 interface 또는 동등한 전략 계약으로 둔다.
- 현재 Haversine 기반 구현은 `DefaultCourseRoutePlanner` 같은 기본 구현체로 분리한다.
- planner 입력은 `CourseStopPoint` 또는 `CourseRoutePlanningRequest`처럼 core domain/use case 타입으로 둔다.
- planner 출력은 우선 `CourseRoute`로 둔다. provider/fallback metadata가 제품 계약에 필요해질 때만 별도 result DTO를 추가한다.
- 미래 AI planner 구현체는 `core-api` 안에 두고, 필요한 경우 `external` concrete client/result DTO를 직접 주입받아 `CourseRoute`로 매핑한다.
- `external` 모듈이 core-api planner interface를 구현하거나 core domain 타입을 의존하게 만들지 않는다.

외부 provider/AI 호출이 들어가는 시점에는 route planning을 DB write transaction 밖에서 수행하도록 create/update 흐름을 분리한다.
이번 계획에서는 외부 provider 구현체를 추가하지 않고, 교체 가능한 planner 경계만 확보한다.

### 3.4 Storage projection

단기적으로는 현재 `CourseMapper` 구조를 유지한다.

- `course_items`는 `item_type`, `attraction_id`, `note_id`를 계속 저장한다.
- `CourseItemRecord`는 write/read id 재조회용 storage contract로 유지한다.
- `CourseItemDetailRecord`는 조회 projection으로 유지하되, title 선택은 service 내부 여러 곳에 흩어지지 않게 한다.

중기 개선 후보:

- `CourseItemDetailRecord`에 `itemTitle` alias를 SQL에서 만들어 projection이 이미 공통 제목을 갖게 한다.
- public item 조회 SQL은 mapper XML에서 attraction/note 공개 조건을 계속 책임진다.
- core domain 변환은 `CourseService`/`AdminCourseService` call path 또는 service-local helper에서만 수행한다.

## 4. 구현 단계

### Phase 0. 현재 계획 정리

- 기존 active 계획 `course_domain_analysis.md`는 `docs/plans/execute/2026-06-23-course-domain-analysis.md`로 이동했다.
- 이 문서는 현재 active 구현 계획이다.

완료 조건:

- `docs/plans/active`에는 현재 실행할 계획만 남는다.

### Phase 1. 회귀 테스트 잠금

변경 전에 현재 동작을 고정한다.

- `CourseRouteTest`
  - target null, position/day/stayMinutes validation 유지
  - segment 개수와 인접 position 정합성 유지
- `CourseRoutePlannerTest` 또는 기본 planner 구현체 테스트
  - 입력 position 정렬 후 1부터 재부여
  - 좌표가 없는 stop은 invalid 처리
- `CourseServiceTest`
  - attraction stop 저장 시 `item_type=ATTRACTION`, `attraction_id`만 저장
  - note stop 저장 시 `item_type=NOTE`, `note_id`만 저장
  - public/active가 아닌 target은 코스에 추가할 수 없음
- `AdminCourseServiceTest`
  - 관리자 코스 생성/수정도 같은 resolve/plan/write 규칙을 따른다.
- `CourseControllerTest`
  - 기존 `items[]` 요청/응답 shape 유지

완료 조건:

- 리팩터링 전후로 public API와 persistence 계약이 바뀌지 않음을 테스트가 설명한다.

### Phase 2. `CourseStopPointResolver` 도입

`CourseService.toCandidate(...)`와 `AdminCourseService.toCandidate(...)`의 attraction/note 조회 분기를 resolver로 이동한다.

예상 흐름:

```text
CourseService.createCourse(course)
  -> courseStopPointResolver.resolveAll(course.route().stops())
  -> courseRoutePlanner.plan(points)
  -> replaceRoute(course.id(), plannedRoute)
```

`AdminCourseService`도 같은 흐름을 사용한다.

구현 세부:

- resolver는 `AttractionMapper`, `NoteMapper`를 직접 주입받는다.
- attraction target은 `existsPublicVisibleById`와 `findByIds`로 조회한다.
- note target은 `existsPublicActive`와 `findById`로 조회한다.
- 실패는 기존과 동일하게 `CoreException(COURSE_INVALID_ITEM)`으로 유지한다.
- resolver는 `CourseStopPoint`를 반환하고 planner 내부 DTO를 만들지 않는다.
- 기존 `CourseRoutePlanner.StopCandidate`는 삭제하거나 planner 구현체 내부 private 개념으로 축소한다.

완료 조건:

- `CourseService`와 `AdminCourseService`는 target type별 좌표 조회 분기를 직접 갖지 않는다.
- route planning은 resolved point 목록만 다룬다.
- resolver 테스트 또는 service 테스트가 attraction/note 공개/활성 검증 실패를 설명한다.

### Phase 3. route planner 전략 경계 정리

현재 내부 알고리즘을 미래 AI 추천 경로 구현체와 교체 가능한 planner 경계 뒤에 둔다.

구현 세부:

- `CourseRoutePlanner`를 전략 계약으로 정리한다.
- 현재 Haversine 기반 알고리즘은 기본 구현체로 분리한다.
- `CourseService`와 `AdminCourseService`는 기본 구현체가 아니라 planner 계약에 의존한다.
- planner 입력은 `CourseStopPoint` 목록 또는 `CourseRoutePlanningRequest`로 정리한다.
- planner 출력 route는 `CourseRoute` 생성/검증을 통과해야 한다.
- `Course`/`CourseRoute`는 planner 구현체를 주입받거나 직접 호출하지 않는다.
- 향후 AI planner는 `core-api` 구현체로 추가하고 `external` concrete client를 사용한다.

완료 조건:

- planner 교체 지점이 명확하다.
- default planner 테스트가 현재 segment 계산 정책을 고정한다.
- `Course`/`CourseRoute`에는 provider 선택, 외부 호출, fallback 정책이 들어가지 않는다.

### Phase 4. target storage 변환 책임 축소

`CourseService`, `AdminCourseService`, DTO에 반복된 target 분기를 줄인다.

우선순위:

1. `CourseStopTarget`에 storage write helper를 추가할지 검토한다.
2. 추가한다면 core domain이 storage Record를 알지 않도록 primitive 값까지만 제공한다.
3. `CourseService.toItemRecord(...)`와 `AdminCourseService.toItemRecord(...)`는 helper를 사용해 `itemType`, `attractionId`, `noteId`를 채운다.
4. `CourseItemResponse`도 같은 target API를 사용해 attraction/note id를 만든다.

가능한 API:

```java
public sealed interface CourseStopTarget {
    CourseStopTargetType type();

    Long id();

    default Long attractionIdOrNull() { ... }

    default Long noteIdOrNull() { ... }
}
```

주의:

- type별 조건문을 완전히 제거하는 것이 목적은 아니다.
- 조건문을 service/use case 흐름에서 target identity 내부나 resolver 경계로 모으는 것이 목적이다.

완료 조건:

- `CourseService`와 `AdminCourseService`에 남는 target 분기는 storage 변환 최소 범위로 제한된다.
- web response 변환도 target의 공통 API를 우선 사용한다.

### Phase 5. read projection 정리

조회 시 title 선택 분기를 줄인다.

선택지:

- A안: `CourseMapper.xml`에서 `case i.item_type when 'ATTRACTION' then a.title when 'NOTE' then n.title end as itemTitle`을 내려준다.
- B안: `CourseItemDetailRecord`에 `itemTitle()` accessor/helper를 둔다.

판단:

- storage Record에 core domain 변환은 금지지만, projection field 자체를 공통 컬럼으로 갖는 것은 허용 가능하다.
- SQL이 이미 left join projection을 소유하므로 A안이 더 직접적이다.
- `coalesce(a.title, n.title)`는 잘못된 row에 양쪽 title이 존재할 때 오류를 숨길 수 있으므로 사용하지 않는다.

완료 조건:

- `CourseService.itemTitle(...)`/`AdminCourseService.itemTitle(...)` 같은 타입 분기 helper가 사라지거나 단일 projection field 사용으로 축소된다.

### Phase 6. route/segment 흐름 유지 검증

현재 코스 구조는 이미 `CourseRoute`와 `CourseRouteSegment`를 도입하고 있으므로, 이번 작업은 segment schema를 새로 흔들지 않는다.

검증할 것:

- `course_route_segments`는 stop id 재조회 이후 저장된다.
- public course 조회에서 비공개 attraction/private note가 제외된다.
- segment가 일부 누락된 public 조회는 기존 정책대로 unplanned route로 복원된다.
- strict 조회 경로에서는 segment 불일치를 invalid로 처리한다.

완료 조건:

- "장소/쪽지 공통 역할" 리팩터링과 planner 전략 경계 정리가 route segment 저장/복원을 깨지 않는다.

## 5. 비목표

이번 계획에서 하지 않는다.

- 장소와 쪽지 테이블 통합
- `course_items` schema의 target 구조 변경
- 공개 범위 정책 변경
- `/api/courses` 요청/응답 breaking change
- 외부 길찾기/AI planner provider 구현체 도입
- course route optimization 제품 정책 변경
- mapper/repository/port 계층 추가

## 6. 검증 계획

코드 변경 시 기본 검증:

```bash
./gradlew :core:core-api:check
```

mapper/XML 또는 fixture schema 변경 시:

```bash
./gradlew :storage:db-core:test
./gradlew :core:core-api:check
```

public API shape가 바뀌지 않아야 하므로 controller test를 우선 확인한다.
실제 런타임이 가능한 경우에는 `/api/courses` 생성/조회 JSON에서 다음을 확인한다.

- `items[].itemType`
- `items[].attractionId`
- `items[].noteId`
- `items[].position`
- `routeSummary`
- `segments`

## 7. 완료 기준

- `CourseService`와 `AdminCourseService`의 route planning 흐름에서 attraction/note 좌표 조회 분기가 사라진다.
- 장소/쪽지 target 검증과 좌표 resolve는 `CourseStopPointResolver` 경계로 모인다.
- planner는 교체 가능한 전략 경계로 유지되고, 기본 구현체가 현재 route segment 계산 정책을 보존한다.
- `Course`/`CourseRoute`는 planner 구현체를 소유하지 않고 route 정합성만 검증한다.
- storage write/read 계약은 기존 `course_items` 구조를 유지한다.
- public API의 기존 `items[]` shape는 유지된다.
- 관련 테스트가 통과한다.
- 완료 보고에는 `CONSTITUTION.md`, `RULES.md`, `core/core-api/AGENTS.md`, `storage/db-core/AGENTS.md` 사용 여부와 검증 결과를 남긴다.
