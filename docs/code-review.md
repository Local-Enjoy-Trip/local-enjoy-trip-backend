# 코드 리뷰 가이드라인 (Code Review Guidelines)

본 문서는 EnjoyTrip 프로젝트의 코드 리뷰 진행 시 필수적으로 점검해야 하는 기준을 정의합니다. 모든 코드 변경 사항은 Pull Request(PR) 승인 전에 아래 점검 항목을 통과해야 합니다.

---

## 1. 헌법 및 프로젝트 규칙 준수 여부
*   **[CONSTITUTION.md](file:///Users/hj.park/projects/local-enjoy-trip-backend/CONSTITUTION.md) 준수 여부:** 
    *   최상위 프로젝트 규칙인 헌법 위반 사항이 없는지 가장 먼저 확인합니다.
*   **[RULES.md](file:///Users/hj.park/projects/local-enjoy-trip-backend/RULES.md) 및 [PRECEDENTS.md](file:///Users/hj.park/projects/local-enjoy-trip-backend/PRECEDENTS.md) 확인:**
    *   각 모듈별 상세 규칙 및 이전에 합의된 선례들을 위반하지 않는지 체크합니다.

## 2. 요청-응답 흐름 최적화 및 중복 검증 제거
*   **중복 검증 제거:**
    *   하나의 API 요청-응답(Request-Response) 흐름 내에서 동일한 조건이나 비즈니스 로직을 중복하여 검증하고 있지 않은지 확인합니다. (예: Controller에서 검증한 내용을 Service나 MyBatis Mapper 호출 시점에 불필요하게 재검증하는 경우)
*   **불필요한 코드 제거:**
    *   실제 사용되지 않는 변수, 중복 계산, 호출되지 않는 메서드 등 무의미한 코드가 포함되어 있는지 점검합니다.

## 3. 적절한 추상화 및 메서드 분리 수준
*   **과도한 추상화 및 분리 지양:**
    *   단지 분리만을 목적으로 불필요하게 인터페이스나 추상 클래스를 도입했는지 점검합니다.
    *   단 한 줄짜리 코드이거나 단순 위임에 불과한 로직을 별도 메서드로 분리하여 코드의 탐색 비용을 높이고 있지 않은지 확인합니다.
*   **가독성을 위한 적절한 분리 지향:**
    *   반대로 하나의 메서드에 너무 많은 로직이 몰려 있어 전체적인 제어 흐름(Flow)을 읽기 어려운 경우가 없는지 체크합니다.
    *   주요 비즈니스 흐름을 나타내는 상위 레벨 메서드는 세부 구현 사항(Low-level detail)을 적절히 분리된 하위 메서드 호출로 캡슐화해야 합니다.

## 4. 명명 규칙 (Naming Conventions)
*   **기술/프레임워크 이름 배제:**
    *   클래스명, 인터페이스명, 변수명 등에 구체적인 기술이나 프레임워크 명칭(예: `MyBatis`, `Postgres`, `Spring`, `Impl` 등)을 직접적으로 노출하고 있지 않은지 점검합니다.
    *   *잘못된 예시:* `MyBatisUserMapper`, `PostgresUserRepository`, `UserRedisDto`
    *   *올바른 예시:* `UserMapper`, `UserRepository`, `UserCacheDto`

## 5. 트랜잭션 범위 및 DB 커넥션 관리
*   **불필요한 `@Transactional` 지양:**
    *   Service 레이어의 클래스 레벨이나 메서드 레벨에 `@Transactional` 어노테이션이 남용되어 있지 않은지 점검합니다.
    *   단순 읽기(Read-only) 작업만 수행하거나 DB 작업이 전혀 없는 단순 유틸리티성 Service 메서드에 트랜잭션이 선언되어 불필요하게 DB 커넥션을 조기에 획득하고 오래 보유하지 않도록 합니다.
    *   *가이드:* 데이터 변경(Write) 작업이 동반되거나, 복수의 데이터 정합성을 유지해야 하는 비즈니스 단위에만 신중하게 `@Transactional`을 부여합니다.

## 6. 도메인 모델 내 VO(Value Object) 분리 및 재사용
*   **VO의 적극적인 재사용 여부 점검:**
    *   개별 primitive 필드로 흩어지기 쉬운 구조화된 데이터는 기존에 정의된 VO를 적극적으로 재사용해야 합니다.
    *   *예시:* 위경도 정보는 개별 `Double latitude`, `Double longitude` 필드로 중복 선언하지 않고, 이미 정의된 [Coordinate](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/vo/Coordinate.java) VO를 사용합니다. (현재 [Note](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/Note.java), [NoteMapPin](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/NoteMapPin.java), [PlaceMapPin](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/PlaceMapPin.java)에 흩어진 개별 필드들을 `Coordinate`로 변환할 수 있습니다.)
    *   *예시:* 평점 평균 및 개수는 개별 필드가 아닌 [RatingStats](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/vo/RatingStats.java)를 사용하여 응집도를 높입니다.
*   **새로운 VO 도출 및 캡슐화:**
    *   항상 같이 생성되고 비즈니스 개념상 하나로 묶이는 필드 집합은 새로운 VO로 분리하여 응집도를 높이고 도메인 불변식을 캡슐화합니다.
    *   *예시 (경로 메트릭):* [CourseStop](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/CourseStop.java) 내의 `Integer distanceToNext`와 `Integer durationToNext`는 다음 목적지까지의 경로 메트릭 정보를 지닌 하나의 개념이므로, `RouteMetric` 혹은 `TransitMetric` VO로 분리할 수 있습니다.
    *   *예시 (이미지 메타데이터):* [Note](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/Note.java) 내의 이미지 속성들 (`imageObjectKey`, `imageUrl`, `imageContentType`)은 `ImageMetadata` 혹은 `ImageReference`와 같은 VO로 그룹화하여 캡슐화할 수 있습니다.

## 7. 도메인 간 결합도 완화 및 생명주기(Lifecycle) 분리
*   **도메인 개념의 비대화(Bloated Domain) 방지:**
    *   하나의 도메인이 다른 도메인의 개념을 직접 소유하거나(예: 닉네임, 프로필 이미지 등), 다른 핵심 도메인들에 강하게 의존하여 변경의 파급력이 커지는 구조를 경계합니다.
*   **구체적인 도메인 끊어내기(Decoupling) 기준:**
    *   **ID 기반의 간접 참조:** 생명주기가 완전히 다르고 부모-자식 관계가 아닌 두 도메인은 직접적인 객체 참조를 지양하고, ID 참조 방식으로 느슨하게 연결해야 합니다.
    *   **도메인 구체 타입 의존성 분리:** 
        *   *예시 (Course와 Attraction/Note):* [CourseStopTarget](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/CourseStopTarget.java)은 sealed interface를 통해 구체 도메인인 `Attraction`과 `Note`를 직접 바인딩하고 있습니다. 코스 도메인에서 관광지나 노트를 완전히 끊어내기 위해, 구체적인 도메인 타입을 알지 못하는 중립적인 타겟 정보 `StopTarget(targetId, targetType)` 구조로 대체하여 결합도를 낮출 수 있습니다.
    *   **표현용/조회용 필드 도메인 제거:**
        *   도메인 엔티티 내부에 화면에 그리기 위한 타 도메인의 속성(예: 작성자의 닉네임, 프로필 이미지 등)이 들어가지 않도록 분리합니다.
        *   *예시:* [NoteMapPin](file:///Users/hj.park/projects/local-enjoy-trip-backend/core/core-api/src/main/java/com/ssafy/enjoytrip/core/domain/NoteMapPin.java)에 들어있는 `authorNickname`, `authorProfileImageUrl` 등은 도메인의 순수 코어 데이터가 아닙니다. 도메인은 `authorMemberId`만 들고 있도록 끊어내고, 화면 조회가 필요한 영역(MyBatis Mapper SQL projection이나 조회용 DTO)에서 조인(Join)을 통해 해당 필드를 제공하도록 분리해야 합니다.
    *   **생명주기 분리를 통한 도메인 분할 (예: 유저와 유저 주소):**
        *   유저(`Member`)가 회원가입/인증 등의 기본 생명주기를 가진다면, 주소 정보가 배송 이력, 주소록 관리 등 독립적인 비즈니스 규칙과 영속성 주기를 지닐 수 있습니다.
        *   유저 엔티티 내부에 주소 정보를 리스트나 큰 덩어리로 집어넣어 유저 도메인을 비대하게 만드는 대신, `Member`와 `MemberAddress`를 완전히 분리하여 `MemberAddress`가 `memberId`를 참조하는 별도의 Aggregate로 설계하면 도메인을 깔끔하게 끊어낼 수 있습니다.

