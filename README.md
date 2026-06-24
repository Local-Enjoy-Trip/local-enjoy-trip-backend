# Local Enjoy Trip Backend

## 구조

- `core/core-api`: Spring Boot 주 실행 모듈. 단일 API entrypoint에서 HTTP/API와 scheduled/background job을 함께 실행한다.
  - API main: `com.ssafy.enjoytrip.EnjoyTripApplication`
  - Web/API 코드는 `com.ssafy.enjoytrip.core.api.web.*`에 둔다.
  - Scheduled/background job 코드는 별도 runtime profile이나 별도 main 없이 일반 API runtime context에서 실행한다.
- `core/core-enum`: `core-api`와 `storage/db-core`가 공유하는 enum 전용 모듈.
- `storage/db-core`: MyBatis mapper/XML, storage Record, Flyway migration 인프라.
- `core`: Gradle namespace parent for `core-api` and `core-enum`; no source-bearing legacy core module.
- `storage`: Gradle namespace parent for `db-core`; no source-bearing legacy storage module.
- `external`: active outbound integration module for third-party API, AI, and MinIO implementations.
- `batch`: 수동/offline Spring Batch runtime.
- `support/logging`, `support/monitoring`: runtime support resources used by active runtimes. Auth support is absorbed into `core-api`; `support/auth` is removed.

`backend/` 래퍼 디렉터리는 제거했다. 새 코드는 프로젝트 루트의 위 모듈 아래에 둔다. Web package는 background-only Scheduled 코드를 소유하지 않고, background job package는 controller/OpenAPI/REST Docs/DTO/response envelope를 소유하지 않는다.

## API

컨트롤러 요청/응답 계약은 반드시 명명된 객체 DTO를 사용한다. `@RequestParam Map`, `@RequestBody Map`, `ApiResponse<Map<...>>`, `Map.of(...)`로 만든 임시 응답 객체는 사용하지 않는다.

- `GET /health`
- `GET /api/db/health`
- `GET /api/route/optimize?points=lat,lng|lat,lng|...`
- `GET /api/route/split-by-day?points=lat,lng|lat,lng|...&days=3`
- `GET /api/members`
- `POST /api/members?action=signup|login|logout|find-password|update|delete`
- `GET /api/notices`
- `POST /api/notices?action=create|update|delete`
- `GET /api/news`
- `GET /api/attractions`
- `GET /api/chargers`
- `GET /api/hotplaces?memberId={memberId}`
- `POST /api/hotplaces?action=create|delete`
- `GET /api/plans?memberId={memberId}`
- `GET /api/boards`
- `POST /api/boards?action=create|update|delete`

Mutation 경로:

- `POST /api/members/signup`, `POST /api/members/login`, `PUT /api/members/me`, `DELETE /api/members/me`
- `POST /api/notices/items`, `PUT /api/notices/{id}`, `DELETE /api/notices/{id}`
- `POST /api/boards/posts`, `PUT /api/boards/{id}`, `DELETE /api/boards/{id}`
- `POST /api/hotplaces/items`, `DELETE /api/hotplaces/{id}`
- `POST /api/plans/items` (JSON), `PUT /api/plans/{id}` (JSON), `PUT /api/plans/{id}/items` (JSON), `DELETE /api/plans/{id}`

### Plans canonical JSON 예시

여행 계획 mutation은 JSON request body만 사용한다. `memberId`는 요청 body에서 받지 않고 인증된 JWT subject를 사용한다.

```http
POST /api/plans/items
Authorization: Bearer <token>
Content-Type: application/json

{
  "id": "p1",
  "title": "서울 여행",
  "startDate": "2026-05-14",
  "endDate": "2026-05-15",
  "budget": 100000,
  "routeItems": [
    {"attractionId": 1, "day": 1, "stayMinutes": 120}
  ]
}
```

## 실행

API 실행:

```powershell
.\gradlew :core:core-api:bootRun
```

OpenTelemetry Java agent로 로컬 관측 실행:

```powershell
.\gradlew :core:core-api:bootRunOtel
```

위 태스크는 `infra/agent/opentelemetry-javaagent.jar`를 자동으로 준비하고 기본 OTLP endpoint를
`http://localhost:4318`로 사용한다. IntelliJ에서는 `.run/Core API OTel.run.xml` 실행 구성을 선택한다.

빌드/검증:

```powershell
.\gradlew :core:core-api:check
.\gradlew :storage:db-core:check
.\gradlew :core:core-api:bootJar
```

생성 결과:

- `core/core-api/build/libs/core-api-1.0.0-SNAPSHOT.jar`
- `core/core-api/build/docs/asciidoc/index.html`

## DB/API Key

DB 접속 정보와 외부 API key는 환경변수로만 주입한다. 실제 값은 README나 커밋된 문서에 적지 않는다.

필수 환경변수:

- `ENJOYTRIP_DB_URL`
- `ENJOYTRIP_DB_USER`
- `ENJOYTRIP_DB_PASSWORD`
- `ENJOYTRIP_TOUR_API_KEY` 또는 `TOUR_API_KEY`
- `EV_CHARGER_API_KEY` 또는 `ENJOYTRIP_EV_API_KEY`

로컬 개발용 샘플은 `.env.example`을 복사한 뒤 개인 환경에 맞게 값을 채워 사용한다.
