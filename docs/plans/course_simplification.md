# 코스(Course) 도메인 단순화 및 격벽 재설계 계획

이 계획서는 코스 도메인의 과도한 기술적 세분화를 제거하고, 본질적인 비즈니스 개념인 **코스(Course)**와 **경유지(CourseStop)**만을 도메인 개념(Concept)으로 남기기 위한 단순화 리팩토링 계획입니다. 

불필요한 부가 연산 모델(`RouteSummary`, `CourseRoute`) 및 이동 수단(`travelMode`) 등은 제외하고, 장소 간 이동 요소를 단순한 상태와 파생 행위로 재설정합니다.

---

## 1. 개편 방향 및 목표
1. **기술적 복잡성 제거:** `CourseRoute`, `CourseRouteSegment`, `RouteSummary` 클래스를 완전히 삭제합니다.
2. **도메인 핵심 개념 축소:** 도메인 개념을 오직 **`Course`**와 **`CourseStop`** 두 가지만으로 통일합니다.
3. **상태(State)와 행위(Behavior)로의 재정의:**
    * 장소 사이의 거리(`distanceToNext`)와 시간(`durationToNext`)은 `CourseStop` 내부의 단순 **상태(State)**로 정의합니다.
    * 불필요한 이동 수단(`travelMode`) 및 복잡한 세그먼트 순서 정합성 체크 로직은 제거합니다.
    * 코스의 전체 거리 및 소요 시간은 실시간으로 stops 상태를 합산하여 연산하는 **파생 행위(Behavior)**로 바꿉니다.

---

## 2. 개편 후 남게 되는 도메인 개념과 상태 (최종 결과물)

### 📦 개념 1: 코스 (Course - Aggregate Root)
사용자가 작성하거나 관리자가 추천(큐레이션)하는 여행 경로 정보의 주체입니다.

*   **상태 (States):**
    *   `id` (String): 코스 고유 식별자
    *   `ownerUserId` (String): 소유자 사용자 ID
    *   `title` (String): 코스 제목
    *   `regionName` (String): 여행 지역명
    *   `visibility` (String): 공개 범위 (`PUBLIC`, `PRIVATE` 등)
    *   `status` (String): 코스 상태 (`READY`, `IN_PROGRESS` 등)
    *   `description` (String): 코스 설명
    *   `coverImageUrl` (String): 커버 이미지 URL
    *   `stops` (List<CourseStop>): 정렬된 경유지 목록
*   **행위 (Behaviors):**
    *   `requireOwnedBy(userId)`: 소유자 일치 여부를 검증하고 실패 시 예외 처리
    *   `totalDistanceMeters()`: 경유지 목록의 `distanceToNext`를 모두 더해 코스 총거리를 계산 (파생 행위)
    *   `totalDurationSeconds()`: 경유지 목록의 `durationToNext`를 모두 더해 코스 총소요시간을 계산 (파생 행위)

### 📦 개념 2: 경유지 (CourseStop - Value Object)
코스 내부에서 하나의 방문 지점(장소) 및 다음 지점까지의 이동 거리를 나타냅니다.

*   **상태 (States):**
    *   `id` (Long): 경유지 식별자
    *   `target` (CourseStopTarget): 경유하는 실질 대상 (관광지 ID 또는 개인 메모 ID)
    *   `position` (int): 코스 내 방문 순서 (1부터 시작)
    *   `day` (int): 코스 내 방문 일차 (1부터 시작)
    *   `memo` (String): 해당 경유지에 남기는 사용자 메모
    *   `stayMinutes` (Integer): 해당 장소에서의 예상 체류 시간
    *   `title` (String): 경유지의 명칭
    *   `distanceToNext` (Integer): 다음 경유지까지의 이동 거리 (미터 단위, 마지막 경유지는 null)
    *   `durationToNext` (Integer): 다음 경유지까지의 이동 소요 시간 (초 단위, 마지막 경유지는 null)
*   **행위 (Behaviors):**
    *   생성 시 비즈니스 무결성 검증 (예: `position > 0`, `day > 0`, `distanceToNext >= 0` 검증)
    *   `withoutStorageId()`: 저장소 식별자가 제거된 경유지 복사본 생성

---

## 3. 리팩토링 영향 범위 및 작업 명세

### 1) 파일 삭제
*   `core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/CourseRoute.java`
*   `core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/CourseRouteSegment.java`
*   `core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/RouteSummary.java`

### 2) 도메인 레이어 수정
*   **`Course.java` 및 `CourseStop.java`**:
    *   위 명세된 상태와 행위를 갖도록 필드 구성 및 생성자 수정.
*   **`CourseStopPointResolver.java` / `CourseOrderPreviewReader.java`**:
    *   `CourseRoute` 의존성을 제거하고 `List<CourseStop>` 기반으로 동작하도록 수정.
*   **`CourseReader.java` / `CourseWriter.java`**:
    *   DB에서 데이터를 읽고 적재할 때 `stops` 리스트에 포함된 `distanceToNext`, `durationToNext` 상태를 매핑하도록 변경.

### 3) 인프라 및 DB 계층 (`storage:db-core`) 수정
*   **데이터베이스 테이블 간소화**:
    *   `course_route_segments` 테이블을 제거하거나, 단순하게 `course_items` 테이블의 컬럼(`distance_to_next`, `duration_to_next`)으로 흡수 통합 검토. (또는 일관성을 위해 매퍼 쿼리 수준에서 `course_route_segments` 조인 구조만 단순화 유지)
*   **MyBatis Mapper 및 Record DTO 수정**:
    *   `travel_mode` 및 `segment_order` 등 세그먼트와 관련된 쿼리 및 변환 로직 제거.
