# Backend Constitution

이 문서는 `backend/`에서 반드시 지켜야 하는 최상위 규칙이다. 세션, 작업, 하위 모듈별 `AGENTS.md`, 임시 계획, 개인 취향 규칙이 이 문서와 충돌하면 이 문서가 우선한다.

## 1. 3계층 규칙 체계

Backend 규칙은 항상 다음 3계층으로 관리한다.

1. `CONSTITUTION.md` — 절대 규칙
   - 아키텍처 경계, 보안, 검증, 변경 절차처럼 항상 지켜야 하는 원칙을 둔다.
   - 예외가 필요하면 먼저 사용자에게 이유와 위험을 명시하고 승인을 받아야 한다.
2. `RULES.md` — 운영 규칙과 현장 템플릿
   - 반복적으로 쓰는 구체적인 작업 규칙, 체크리스트, PR/테스트/마이그레이션 템플릿을 둔다.
   - `CONSTITUTION.md`를 구체화할 수는 있지만 완화할 수 없다.
3. `PRECEDENTS.md` — 사례 축적소
   - 아직 일반 규칙으로 승격하기 전의 사례, 판단, 예외, 반복 패턴을 기록한다.
   - 같은 유형의 사례가 3번 반복되면 `RULES.md`로 승격한다.

## 2. 모듈 경계는 반드시 유지한다

Backend는 `web`, `worker`, `core`, `storage`, `external` 모듈 경계를 유지한다.

- `web`: Spring Boot HTTP/API executable, security/config, controller, request/response DTO, exception/response handling, OpenAPI/REST Docs.
- `worker`: Spring Boot background executable, Kafka/Scheduled/background ingress, retry/error handling, outbox/async processing configuration.
- `core`: domain model, service/application logic, repository port/interface.
- `storage`: JPA entity, Spring Data repository, persistence adapter, migration, jOOQ query.
- `external`: third-party API client, external DTO parsing, outbound integration.

`web`과 `worker`는 Spring Boot 조립 모듈이므로 실행 애플리케이션 구성을 위해 Gradle 의존성으로 `storage` 구현 모듈을 포함할 수 있다.
다만 이 의존성은 bean 조립과 런타임 구성 목적에 한정하며, executable module 소스가 persistence 구현 타입을 직접 참조하는 것은 금지한다.

금지:

- `backend/web/src/main` 또는 `backend/worker/src/main`에서 `com.ssafy.enjoytrip.storage.*` import/reference 금지.
- Controller 또는 worker ingress가 repository를 직접 호출하는 것 금지.
- `core`가 `web`, `worker`, `storage`, `external`, JPA entity, web DTO에 의존하는 것 금지.
- persistence 상세 구현을 `web`, `worker`, `core`로 끌어올리는 것 금지.
- Kafka/Scheduled/background worker 코드를 `web`에 두는 것 금지.
- Controller/API/REST Docs/REST response DTO 코드를 `worker`에 두는 것 금지.

기본 HTTP 흐름:

```text
controller -> service -> repository interface -> storage adapter
```

기본 worker 흐름:

```text
worker ingress -> service/processor -> repository interface -> storage adapter
```

## 3. 구체 운영 규칙은 RULES.md에 둔다

`CONSTITUTION.md`에는 backend의 최상위 원칙과 경계만 둔다. 다음처럼 현장에서 바로 적용하는 구체 규칙은 `RULES.md`에 둔다.

- API DTO 계약 세부 규칙
- JPA/jOOQ/JdbcTemplate/native query 선택 규칙
- 인증/인가/token/redirect 세부 보안 규칙
- migration, 테스트, 완료 보고 템플릿

`RULES.md`는 이 문서를 완화할 수 없으며, 이 문서의 원칙을 실행 가능한 형태로 구체화한다.

## 4. 외부 검색 전 모듈 AGENTS.md를 먼저 확인한다

외부 검색, 온라인 문서 확인, 웹 검색을 하기 전에는 반드시 현재 초점이 맞춰진 모듈 또는 수정하려는 모듈의 `AGENTS.md`를 먼저 찾고 읽는다.

- 수정 대상 파일이 속한 가장 가까운 모듈 디렉터리부터 `backend/`까지 올라가며 적용 가능한 `AGENTS.md`를 확인한다.
- 적용 가능한 `AGENTS.md`가 있으면 그 지침을 먼저 따른다. 단, 이 문서와 충돌하면 `CONSTITUTION.md`가 우선한다.
- 적용 가능한 `AGENTS.md`가 없거나, 해당 지침만으로 해결할 수 없는 최신/외부 정보가 필요할 때만 외부 검색을 사용한다.
- 완료 보고에는 외부 검색 전에 확인한 `AGENTS.md` 또는 없음을 간단히 남긴다.

## 5. 변경 전후 검증은 필수다

Backend 변경은 가능한 한 변경 범위에 맞는 테스트로 검증한다.

기본 우선순위:

1. 변경 모듈의 targeted test
2. 관련 module test
3. `:backend:web:check`, `:backend:worker:check` 또는 전체 backend check
4. 검증 불가 시 이유와 대체 검증을 명시

완료 보고에는 반드시 다음을 포함한다.

- 변경 파일
- 지킨 규칙 또는 참고한 규칙 계층
- 실행한 검증 명령과 결과
- 남은 위험 또는 미검증 항목

## 6. 규칙 변경 절차

- 절대 규칙 변경은 `CONSTITUTION.md`를 수정해야 하며, 이유를 명확히 남긴다.
- 반복 작업 규칙은 먼저 `PRECEDENTS.md`에 사례로 기록한다.
- 같은 유형의 사례가 3번 반복되면 `RULES.md`로 승격한다.
- 승격 시 원래 사례의 링크/요약을 `PRECEDENTS.md`에 유지한다.

## 7. Java 타입은 반드시 import로 선언한다

Java 코드에서 `java.util.ArrayList`, `java.util.List`, `java.time.LocalDateTime`처럼 fully qualified class name을 코드 본문에 직접 쓰지 않는다.

- 필요한 타입은 파일 상단에 `import`로 선언한다.
- 코드 본문, 메서드 시그니처, 필드 선언, 제네릭 타입, 생성자 호출에 fully qualified class name을 남기지 않는다.
- 이름 충돌이 생겨도 fully qualified class name으로 때우지 않는다. 충돌하지 않도록 타입 선택, 책임 분리, 변수명/클래스명 조정을 먼저 한다.
- 새로 작성하거나 수정한 Java 코드에 fully qualified class name이 남아 있으면 완료된 변경으로 보지 않는다.
