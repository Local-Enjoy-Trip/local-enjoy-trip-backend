# Monolithic Core API Migration — 구현 체크포인트

## 현재 결론

이 워크트리는 더 이상 Phase 1 placeholder만 있는 상태가 아니다. 지정 계획
`.omx/plans/monolithic-core-api-deliberate-migration-plan.md` 기준으로 다음 target module에 실제 source/resource가 들어갔다.

- `:core:core-api`
- `:core:core-enum`
- `:storage:db-core`

추가 결정으로 별도 `core-worker` 모듈은 만들지 않고, 기존 `app:worker` 코드는
`core-api` 내부 `com.ssafy.enjoytrip.core.api.worker.*` 패키지로 흡수한다.

기존 `main` 워크트리는 건드리지 않았고, 작업 기준은
`/Users/hj.park/projects/local-enjoy-trip-backend-monolithic-core-api-migration`의
`ralph/monolithic-core-api-migration` 브랜치다.

## 이전 구현이 헷갈렸던 이유

초기 체크포인트는 계획의 Phase 1까지만 반영한 상태였다.

- `settings.gradle`에 새 module include만 추가됐다.
- `core-api`, `core-enum`, `db-core`의 `build.gradle`만 존재했다.
- 실제 web/core/storage/support source 이동은 없었다.
- `core-api:compileJava`가 `NO-SOURCE`였기 때문에 target 구조가 동작한다고 볼 수 없었다.

그래서 “core 모듈을 왜 이렇게 했는지” 알기 어려운 상태가 맞았다.

## 이번 구현에서 실제로 반영한 것

### 1. `core-api` 실제 source 구성

`core/core-api/src/main/java` 아래로 다음 source를 이관했다.

- 기존 `app/src/main/java`의 boot application entrypoint
- 기존 `app/web/src/main/java`의 controller, API interface, request/response DTO, web config, exception handler
- 기존 `app/web/src/test/java`의 web/service/support/security test
- 기존 `app/web/src/docs`의 REST Docs asciidoc source
- 기존 `app/worker/src/main/java`의 legacy async consumer/error handler
- 기존 `app/worker/src/test/java`의 worker behavior test
- 기존 `core/src/main/java`의 domain/application service/repository interface/command/query type
- 기존 `support:error`, `support:auth`, `support:common`
- 기존 storage repository 구현체 `storage/src/main/java/.../storage/repository/**`
- 기존 external 계약은 `core-api`에 남기고, 구현체는 active `external/src/main/java/**`에 둔다.

주요 package mapping:

| 기존 package | target package |
| --- | --- |
| `com.ssafy.enjoytrip.web.*` | `com.ssafy.enjoytrip.core.api.web.*` |
| `com.ssafy.enjoytrip.config.*` | `com.ssafy.enjoytrip.core.api.config.*` |
| `com.ssafy.enjoytrip.domain.*` | `com.ssafy.enjoytrip.core.domain.*` |
| `com.ssafy.enjoytrip.service.*` | `com.ssafy.enjoytrip.core.domain.service.*` |
| `com.ssafy.enjoytrip.repository.*` | delete core-domain repository ports; use `storage:db-core` entity/JPA/jOOQ directly from core-api service/helper code; external provider contracts move to `core.domain.external.*` |
| `com.ssafy.enjoytrip.dto.command.*` | `com.ssafy.enjoytrip.core.domain.command.*` |
| `com.ssafy.enjoytrip.dto.query.*` | `com.ssafy.enjoytrip.core.domain.query.*` |
| `com.ssafy.enjoytrip.support.error.*` | `com.ssafy.enjoytrip.core.support.error.*` |
| `com.ssafy.enjoytrip.support.response.*` | `com.ssafy.enjoytrip.core.support.response.*` |
| external implementation classes | active `external` module under `com.ssafy.enjoytrip.external.*` |
| `com.ssafy.enjoytrip.notification.*` | `com.ssafy.enjoytrip.core.api.worker.notification.*` |

`EnjoyTripApplication`은 root package `com.ssafy.enjoytrip`에 둔다. 기본 component scan이
`core.api`, `core.domain`, `storage.db.core` 하위 package를 모두 포함하므로 별도 `scanBasePackages`를 두지 않는다.

Worker runtime은 같은 Gradle module 안의 별도 main class로 둔다.

```text
com.ssafy.enjoytrip.core.api.worker.EnjoyTripWorkerApplication
```

`bootJar`/`bootRun`의 기본 main class는 API entrypoint로 고정하고, worker 실행은
`:core:core-api:bootRunWorker` task를 사용한다. worker entrypoint는 Spring profile을
활성화하지 않고 non-web runtime으로 실행한다.

### 2. `db-core` 실제 source/resource 구성

`storage/db-core/src/main` 아래로 persistence 구현 세부를 이관했다.

- JPA entity: `com.ssafy.enjoytrip.storage.db.core.entity.*`
- Spring Data JPA repository: `com.ssafy.enjoytrip.storage.db.core.jpa.*`
- storage auto-configuration: `com.ssafy.enjoytrip.storage.db.core.StorageConfiguration`
- Flyway migration resources
- jOOQ codegen config/resources

중요한 사이클 차단:

- core-domain repository 구조는 target에서 제거한다.
- `core-api` 서비스는 storage entity/JPA/jOOQ types를 직접 import한다; provider/gateway/generator 계약은 `core.domain.external.*`, 구현체는 `external` module에 둔다.
- 따라서 target dependency는 `core-api -> db-core`, `db-core -> core-enum`이고,
  `db-core -> core-api` 역방향은 없다.

### 3. `core-enum` 실제 source 구성

`db-core` entity/JPA가 실제로 공유하는 enum 4개를 `core-enum`에 분리했다.

- `FriendshipStatus`
- `legacy notification status enum`
- `NotificationReferenceType`
- `NotificationType`

이 enum들은 기존 domain package와의 import churn을 줄이기 위해 현재 FQCN을
`com.ssafy.enjoytrip.core.domain.*`로 유지한다. 소유 module만 `core-enum`이다.

### 4. external/core cycle 처리

초기 계획은 `external`을 별도 module로 유지하되 `core` 의존을 제거하라고 했다.
이번 구현에서는 target runtime을 먼저 green으로 만들기 위해 다음 절충을 택했다.

- `core-api` compileClasspath가 `external` 구현체를 끌고 오지 않게 유지한다.
- 기존 external 구현체는 active `external` module로 분리하고 새 core-api 계약을 구현한다.
- 따라서 target `core-api` compileClasspath에는 `external`이 없고, runtimeClasspath에서만 `external` 구현체를 조립한다.

검증된 dependency graph 요약:

```text
Project ':core:core-api'
+--- project :core:core-enum
+--- project :storage:db-core
|    +--- project :core:core-enum
```

## 삭제한 것과 남긴 것

`app` 계열은 `core-api`에 흡수한 뒤 target Gradle graph에서 제거한다.

- `app`
- `app:web`
- `app:worker`

별도 active surface로 유지하는 module:

- `core`
- `storage`
- `external`
- `support:*`

`batch`는 계획대로 별도 runtime으로 보존한다.

## 검증 결과

실행 명령:

```bash
rtk bash -lc "./gradlew :core:core-api:check :storage:db-core:check :core:core-api:bootJar :external:compileJava && git diff --check"
```

결과:

- `BUILD SUCCESSFUL`
- `:core:core-api:check` 통과
- `:storage:db-core:check` 통과
- `:core:core-api:bootJar` 통과
- legacy `:external:compileJava` 통과
- `git diff --check` 통과

Worker/test 흡수 후 추가 검증:

```bash
rtk bash -lc "./gradlew :core:core-api:test --console=plain"
```

결과:

- `BUILD SUCCESSFUL`
- 102 tests completed
- 기존 worker `legacy async consumer test`가 `core-api` test suite 안에서 실행됨

추가 확인:

```bash
rtk bash -lc "./gradlew -q :core:core-api:dependencies --configuration compileClasspath | rg ':(core|storage|external|support|app)' || true"
```

결과:

- `core-api`는 `core-enum`, `db-core`만 project dependency로 가진다.
- `core-api` compileClasspath에 legacy `core`, `external` 구현체, `support:*`, `app:*`가 없다. `external`은 runtimeClasspath에서만 조립된다.

## 남은 위험과 다음 단계

1. 실제 DB/Redis/API key가 필요한 bootRun + HTTP JSON 검증은 아직 수행하지 않았다.
   - 현재 증거는 compile/check/bootJar 수준이다.
2. `batch`는 계획대로 별도 runtime으로 보존해야 한다.
3. external은 별도 Gradle module로 유지한다. core-api가 구현체 타입을 직접 import하지 않도록 dependency proof를 계속 확인한다.
