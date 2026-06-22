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
