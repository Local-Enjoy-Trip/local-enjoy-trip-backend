# Project Rules

`RULES.md`는 `CONSTITUTION.md`를 현장에서 실행하기 위한 운영 규칙과 템플릿이다. 절대 규칙은 `CONSTITUTION.md`가 우선한다.

## 1. 작업 시작 체크리스트

프로젝트 서버/runtime 작업을 시작할 때 확인한다.

- [ ] 변경 대상이 active module(`core:core-api`, `core:core-enum`, `storage:db-core`, `external`, `batch`, `support:*`)인지 식별했다.
- [ ] public API 계약 변경 여부를 확인했다.
- [ ] DB schema 변경 여부를 확인했다.
- [ ] 인증/인가/secret/token/redirect 관련 보안 영향이 있는지 확인했다.
- [ ] 기존 사례가 `PRECEDENTS.md`에 있는지 확인했다.

## 2. 모듈별 AGENTS.md 우선 읽기

프로젝트 루트 아래 특정 모듈을 수정할 때는 해당 모듈의 `AGENTS.md`를 먼저 읽고 따른다.

필수 확인 대상:

- `core/core-api/AGENTS.md` — API/worker executable, web/controller/DTO, worker ingress, storage entity/JPA/jOOQ, external contract 수정 전
- `core/AGENTS.md` — core 모듈 수정 전
- `storage/AGENTS.md` — storage 모듈 수정 전
- `storage/db-core/AGENTS.md` — db-core entity/JPA/migration/jOOQ 수정 전
- `external/AGENTS.md` — external reference directory 수정 전
- `batch/AGENTS.md` — batch job/launcher/config 수정 전

운영 규칙:

- 모듈 내부 파일을 수정하기 전 해당 모듈 `AGENTS.md`의 역할, 금지사항, 검증 기준을 확인한다.
- 여러 모듈을 동시에 수정하면 수정 대상 모든 모듈의 `AGENTS.md`를 확인한다.
- 모듈 `AGENTS.md`가 `CONSTITUTION.md`와 충돌하면 `CONSTITUTION.md`를 따른다.
- 모듈 `AGENTS.md`가 이 `RULES.md`보다 더 구체적인 구현 규칙을 제공하면 모듈 `AGENTS.md`를 따른다.
- 완료 보고에는 어떤 모듈 `AGENTS.md`를 확인했는지 적는다.

이 문서에는 모듈별 세부 구현사항을 중복해서 적지 않는다. 세부 구현 규칙은 각 모듈의 `AGENTS.md`가 소유한다.

## 3. API 계약 규칙

Controller request/response 계약은 이름 있는 DTO로 표현한다.

금지:

- `Map`, `Map.of(...)`
- `@RequestParam Map`
- `@RequestBody Map`
- `ApiResponse<Map<...>>`

예외적 내부 계산에는 Map을 쓸 수 있지만, public controller 계약에는 노출하지 않는다.

## 4. 저장소 기술 선택 규칙

- 단순 CRUD와 단순 조회는 Spring Data JPA를 사용한다.
- 동적 조건, join, projection, PostGIS, 복잡한 native SQL은 jOOQ를 사용한다.
- `JdbcTemplate`은 사용하지 않는다.
- JPA `@Query(nativeQuery = true)` 기반 native mutation은 금지한다.
- schema 변경은 Flyway migration으로 남긴다.

## 5. 보안과 인증 규칙

- 보호 API는 앱에서 발급한 JWT를 기준으로 인증한다.
- 외부 provider token은 provider 연동 과정의 입력으로만 사용하고 앱 API 인증 수단으로 사용하지 않는다.
- secret, API key, OAuth client secret은 source에 hardcode하지 않는다.
- Java/Kotlin 코드에서 `System.getenv(...)` 또는 `System.getProperty(...)`로 secret/config를 직접 읽지 않는다.
- 환경 변수와 system property fallback은 `application.yml`이 `${ENV_VAR:${OTHER_ENV_VAR:}}` 형태로 받아
  `enjoytrip.*` 설정 property로 노출한다. 코드는 `@Value`, `@ConfigurationProperties`, 또는
  Spring `Environment`로 그 property만 주입받는다.
- redirect URL은 allowlist/config 기반으로 고정하고 사용자 입력 redirect passthrough를 허용하지 않는다.
- token을 URL query/fragment, log, error message에 노출하지 않는다.

## 6. 인증/보안 작업 템플릿

인증/인가 변경 시 다음을 문서화한다.

- 인증 주체: app JWT, OAuth provider, session/cookie 중 무엇인가
- token 발급자와 검증자
- token 저장 위치
- redirect URL allowlist
- failure response에 노출되는 정보
- replay/race 방지 전략

금지:

- access token을 URL query/fragment에 싣기
- secret hardcoding
- provider token을 app API 인증 수단으로 허용
- 사용자 입력 redirect를 그대로 신뢰

검증 예:

```bash
./gradlew.bat :core:core-api:test --tests '*Security*'
./gradlew.bat :core:core-api:test --tests '*Member*'
```

## 7. DB migration 템플릿

Migration 작성 시:

- 파일명: `V{number}__{description}.sql`
- idempotent 여부와 기존 데이터 영향 확인
- unique 제약 추가 전 duplicate 데이터 가능성 확인
- FK 추가 전 orphan 데이터 가능성 확인
- rollback이 필요하면 수동 rollback SQL을 PR/작업 메모에 남김

검증:

```bash
./gradlew.bat :storage:db-core:test
```

## 8. PRECEDENTS 승격 규칙

`PRECEDENTS.md`의 같은 유형 사례가 3번 반복되면 다음 절차로 `RULES.md`에 승격한다.

1. 반복 사례 3개의 제목과 날짜를 확인한다.
2. 공통 원칙을 한 문장으로 정리한다.
3. `RULES.md`에 실행 가능한 체크리스트 또는 템플릿으로 추가한다.
4. `PRECEDENTS.md`에는 `승격됨` 상태와 승격된 RULES 섹션 링크를 남긴다.

## 9. 테스트와 운영 코드 오염 금지 규칙

테스트의 편의성을 위해 운영 코드를 오염시키거나 설계를 망가뜨리지 않는다.

- **한글 DisplayName 필수**: JUnit 테스트 메서드를 작성하거나 수정할 때는 `@DisplayName`으로 테스트 의도와 검증 내용을 한글로 설명한다.
  영어 메서드명만으로 테스트 의미를 추측하게 만들지 않는다.
- **생성자 오염 금지**: 테스트에서 객체를 쉽게 생성하기 위해 운영 코드에 불필요한 생성자(예: 테스트 전용 파라미터 생성자 등)를 다수 선언하지 않는다.
- **PSA 우선**: HTTP, MVC, Security, DB, Cache처럼 Spring이 Portable Service Abstraction과 테스트 지원을 제공하는 영역은
  `MockMvc`, `MockRestServiceServer`, `spring-security-test`, Testcontainers,
  Spring Data/JPA 테스트 slice 등 공식 테스트 표면을 먼저 사용한다.
  운영 코드에 테스트 전용 분기, profile 조건, setter, sleep/timeout 우회 플래그를 추가하지 않는다.
- **DI 우선**: 외부 클라이언트, 시간/랜덤/ID 생성, encoder/decoder, repository, gateway처럼 교체 가능한 협력자는 constructor DI와
  `@ConfigurationProperties`/`@Value` 주입으로 경계를 만든다. 테스트는 그 경계에 fake/mock/stub bean 또는 테스트용 구현체를 주입한다.
- **인터페이스 활용**: 복잡한 의존성을 모킹하거나 테스트 대역을 만들어야 한다면, 해당 책임을 인터페이스로 추출하고 다형성을 활용하여 테스트용 구현체를 작성한다.
- **리플렉션은 최후 수단**: JPA lifecycle/audit처럼 생성·주입 경계가 아니라 persistence framework 상태 자체를 검증하는 경우를 제외하고,
  테스트가 운영 객체의 private field를 리플렉션으로 갈아끼우는 방식은 피한다. 먼저 PSA와 DI로 테스트 가능한 경계를 만든다.
- **캡슐화 유지**: 테스트 코드에서 필드나 상태를 검증하기 위해 무분별한 `setter`나 `@Setter`, 혹은
  불필요한 getter를 public으로 열지 않는다. 필요 시 리플렉션이나 테스트 유틸을 활용하거나 패키지
  private 수준으로 제한한다.

## 10. 코드 가독성 줄바꿈 규칙

긴 한 줄 코드는 리뷰와 유지보수를 방해하므로 작성하지 않는다.

- 메서드 선언, 생성자 호출, 메서드 호출 인자가 길어지면 인자별로 줄바꿈한다.
- `stream`, `builder`, `HttpRequest`, `jOOQ` 체이닝은 단계별로 줄바꿈한다.
- 메서드 내부에서 논리적인 흐름 단위(예: `if`, `for`, `while`, `try-catch` 등 제어문이 시작/종료되는 경계, 또는 비즈니스 로직의 변환/처리 단계가 바뀌는 지점)마다 빈 줄(Blank Line)을 삽입하여 시각적으로 흐름을 분리한다.
- SQL text block 내부도 한 줄에 과도한 컬럼/값을 몰아넣지 말고 논리 단위로 줄바꿈한다.
- 테스트 코드도 운영 코드와 동일하게 가독성을 우선한다. fixture 생성자가 길면 필드 단위로 줄바꿈한다.
- 원칙적으로 새로 작성하거나 수정한 Java/Kotlin 코드에는 110자를 넘는 줄을 남기지 않는다.
- 예외가 필요하면 URL, 긴 상수, 외부 스펙 문자열처럼 줄바꿈이 오히려 의미를 흐리는 경우로 제한한다.

## 11. core 유스케이스 흐름 메서드명 규칙

`core` 모듈은 비즈니스 로직과 application 흐름을 담당하므로, 유스케이스의 상위 메서드는 구현 디테일보다 업무 절차가 먼저 읽히게 작성한다.

원칙:

- public service 메서드나 복잡한 application 흐름의 상위 메서드는 메서드 이름만 따라 읽어도 유스케이스 순서를 알 수 있어야 한다.
- 상위 메서드는 `validate...`, `find...`, `create...`, `save...`, `record...`,
  `return...`처럼 업무 의도가 드러나는 단계 호출로 구성한다.
- `validate...` 단계는 인증 주체, 소유권, 저장된 상태 존재 여부, 도메인 불변식, 교차 리소스 비즈니스 규칙처럼
  `core`가 책임지는 규칙일 때만 사용한다.
- HTTP query/body 모양, raw string parsing, request DTO의 null/blank/range 검증, batch job parameter 존재 여부,
  단순 blank-to-null/default 치환은 `app` 또는 `batch` ingress 경계에서 처리한다.
- `core` service가 `String raw`, `parse...`, `split(...)`, `trim/strip` 기반 요청 해석, 반복되는 `null`/`isBlank`
  default 분기를 갖기 시작하면 먼저 command/value object 경계로 옮길 수 있는지 검토한다.
- `core`에 남길 수 있는 방어 코드는 "HTTP가 아닌 다른 caller가 호출해도 여전히 필요한가",
  "인증 사용자나 저장 상태를 알아야 판단할 수 있는가", "도메인 값 자체를 유효하게 만드는가"에 답할 수 있어야 한다.
- route `points` 같은 구조화된 query string, legacy form JSON blob, plan request default, batch `sourceVersion` 같은
  job parameter는 `core-api`의 web/worker ingress 또는 `batch`의 DTO/config에서 typed command로 변환한 뒤
  해당 모듈의 service에 넘긴다.
- repository/external/runtime 예외를 잡아 fallback 데이터를 반환하는 `core` 흐름은 무음 방어 코드로 두지 않는다.
  fallback이 제품 정책이면 로그, 결과 metadata, 응답 contract, 또는 문서화된 caller contract 중 하나로 실패 사실을 관측 가능하게 한다.
- external provider, runtime 예외는 명시적인 제품 fallback 정책이 없으면 `core` service에서 잡지 않는다.
  예외는 전역 예외 처리까지 전파해 서버 로그에 원인과 stack trace를 남기고, HTTP client에는 표준 내부 오류 응답만 반환한다.
  클라이언트에 원본 예외 메시지를 그대로 노출하지 않는다.
- 외부 연동 실패나 provider 실패를 `CoreException`으로 감싸지 않는다. `CoreException`은 비즈니스 규칙 위반이나
  core가 소유한 application error에만 사용한다. 별도 HTTP status, error type, log level 분류가 필요할 때만
  외부/port 전용 예외나 전역 핸들러 매핑을 추가한다.
- 하위 private 메서드는 각 단계의 구현 세부사항을 숨기되, `process`, `handle`, `doWork`, `check`처럼 의미가 흐린 이름만으로 추출하지 않는다.
- 단순 repository 위임처럼 흐름이 없는 메서드는 억지로 쪼개지 않는다. 분리는 유스케이스 흐름을 더 잘 읽히게 할 때만 한다.
- 조건 분기, 실패 기록, 보상/로그 기록, 외부 gateway 호출이 섞이는 core 서비스는 먼저 회귀 테스트로 동작을 잠근 뒤 단계 이름 중심으로 정리한다.

예시:

```java
public Member login(String userId, String password) {
    Member member = findAuthenticatableMember(userId, password);
    upgradeLegacyPasswordIfNeeded(member, password);
    recordLogin(member);
    return member;
}
```

### 11.1 storage entity -> core domain 변환 규칙

monolithic `core-api` 전환 구조에서는 `core-api` service가 `storage:db-core` entity/JPA 타입을 직접 사용할 수 있다.
따라서 entity를 domain model로 바꿀 때 별도 mapper 계층이나 service-local `toModel`/`toDomain` helper를 두지 않는다.

- 조회 결과를 domain model로 반환해야 하면 service call path에서 `new DomainModel(...)`로 직접 생성한다.
- `stream().map(this::toModel)`, `Optional.map(this::toModel)`, `private toModel(Entity entity)` 패턴은 새로 만들지 않는다.
- `storage:db-core` entity 내부에 core-api domain model을 반환하는 `toModel`/`toDomain` 메서드를 두지 않는다.
  `db-core`는 JPA entity와 persistence infrastructure를 소유하고, core-api domain model 생성 책임은 service call path에 남긴다.
- 같은 entity를 여러 유스케이스에서 반환하더라도 mapper 계층을 추가하기 전에 반환 필드와 caller contract가 정말 같은지 먼저 확인한다.
  중복 제거보다 변환 위치와 의존 방향을 명확히 유지하는 것을 우선한다.

### 11.2 web request DTO -> core service 전달 규칙

`core-api` web request DTO는 HTTP request shape, validation, trimming/defaulting을 소유한다.
하지만 그 DTO나 web 요청을 감싸기 위한 command record를 `core.domain.service` 경계 아래로 그대로 넘기지 않는다.

- Controller는 request DTO에서 값을 정리한 뒤 service에 `domain model`, 기존 core query/value object, 또는 명시적으로 풀어낸
  primitive/value 인자를 넘긴다.
- `core.domain.command.*`처럼 web 요청 필드를 운반하기 위한 wrapper record를 새로 만들지 않는다.
- 요청이 이미 domain model 하나로 표현되면 request/controller 경계에서 `new DomainModel(...)` 또는 domain factory로 만든 뒤 넘긴다.
- 일부 필드만 필요한 유스케이스는 `command.id()`, `command.title()`처럼 service 내부에서 꺼내지 말고, controller에서
  `service.method(id, title, ...)` 형태로 명시적으로 넘긴다.
- service 내부에는 HTTP request DTO import, web DTO import, web-only command import가 남아 있으면 안 된다.
- raw string parsing, blank/null/default 정리는 web request DTO가 처리하되, 저장된 상태/소유권/도메인 불변식 검증은 service/domain이 처리한다.

## 12. 실제 API JSON 응답 검증 규칙

이 repo는 백엔드만 있으므로 화면을 열어 검증하는 것을 완료 기준으로 삼지 않는다.
기능을 추가하거나 수정한 뒤에는 테스트 통과와 별도로, 가능한 한 실제 실행 중인 백엔드에 HTTP 요청을 보내 응답 JSON을 확인한다.

원칙:

- public API를 추가하거나 수정했다면 `curl`, `httpie`, Postman, REST Client 등으로 실제 endpoint를 호출한다.
- 조회 기능은 seed/local DB/test fixture 등 실제 저장소에서 읽힌 데이터가 `data` 아래에 기대한 shape와 값으로 내려오는지 확인한다.
- 생성/수정/삭제 기능은 요청 후 조회 API를 다시 호출해 변경 결과가 JSON 응답에 반영되는지 확인한다.
- 공통 envelope는 최소한 `success`, `data`, `error` 필드가 의도대로 나오는지 확인한다.
- 인증이 필요한 API는 실제 또는 테스트용 JWT를 사용해 성공/실패 응답을 모두 확인한다.
- 외부 API, Redis, DB처럼 런타임 의존성이 있는 기능은 mock 테스트만으로 완료 처리하지 않는다. 로컬에서 의존성을 띄울 수 없으면 이유와 대체 검증을 완료 보고에 명시한다.
- 응답 검증에는 `jq`나 JSONPath를 사용해 핵심 필드를 확인한다. 단순히 HTTP 200만 확인하지 않는다.

권장 검증 예:

```bash
curl -s 'http://localhost:8080/api/attractions?sidoCode=1&keyword=%EA%B6%81' | jq '.success, .data.attractions[0]'
curl -s 'http://localhost:8080/api/weather/briefings' | jq '.success, .data.weather'
curl -s 'http://localhost:8080/api/plans?userId=ssafy' | jq '.success, .data.plans'
```

완료 보고에는 다음을 남긴다.

- 실행한 서버 또는 compose profile
- 호출한 HTTP method/path/query/body
- 사용한 인증 정보의 종류
- 확인한 JSON 필드와 실제 결과 요약
- 실제 요청 검증을 못 했다면 못 한 이유와 대체 검증

## 13. 완료 보고 템플릿

```text
변경 파일:
- ...

적용 규칙:
- CONSTITUTION.md: ...
- RULES.md: ...

검증:
- 명령: ...
- 결과: ...
- 실제 API 요청: ...
- 응답 JSON 확인: ...

남은 위험:
- ...
```
