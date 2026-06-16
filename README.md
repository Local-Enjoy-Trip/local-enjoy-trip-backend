# Local Enjoy Trip Backend

## 구조
- `backend/app`: 실행 애플리케이션을 묶는 source-free 조립/네임스페이스 모듈
- `backend/app/web`: Spring Boot HTTP/API 진입점, REST controller, DTO, validation, security, REST Docs 테스트
- `backend/app/worker`: Kafka/Scheduled/background worker 진입점, outbox/async 처리와 retry/error handling
- `backend/core`: domain model, repository interface 같은 application contract
- `backend/storage`: JPA entity, Spring Data repository, JPA-backed adapter, DB model, PostgreSQL/PostGIS schema 관리
- `backend/external`: 한국관광공사 Tour API, EV 충전소 API, 뉴스 RSS client

`backend/app` 루트에는 업무 소스를 두지 않고, 실행 경계는 `backend/app/web`과 `backend/app/worker` 아래에만 둔다. 두 실행 모듈 소스에서는 `com.ssafy.enjoytrip.storage.*`를 import하면 안 된다. 저장소 구현은 전부 `backend/storage`에 두고, `backend:app:web:check`와 `backend:app:worker:check`의 `forbidStorageReferences`가 executable module의 storage 구현 참조를 차단한다. Kafka/outbox worker 코드는 `backend/app/worker`에 두고, controller/API/REST Docs 코드는 `backend/app/web`에 둔다.

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
- `GET /api/hotplaces?userId={userId}`
- `POST /api/hotplaces?action=create|delete`
- `GET /api/plans?userId={userId}`
- `GET /api/boards`
- `POST /api/boards?action=create|update|delete`

Mutation 경로:
- `POST /api/members/signup`, `POST /api/members/login`, `PUT /api/members/{userId}`, `DELETE /api/members/{userId}`
- `POST /api/notices/items`, `PUT /api/notices/{id}`, `DELETE /api/notices/{id}`
- `POST /api/boards/posts`, `PUT /api/boards/{id}`, `DELETE /api/boards/{id}`
- `POST /api/hotplaces/items`, `DELETE /api/hotplaces/{id}`
- `POST /api/plans/items` (JSON), `PUT /api/plans/{id}` (JSON), `PUT /api/plans/{id}/items` (JSON), `DELETE /api/plans/{id}`


### Plans canonical JSON 예시

여행 계획 mutation은 JSON request body만 사용한다. `userId`는 요청 body에서 받지 않고 인증된 JWT subject를 사용한다.

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
```powershell
.\gradlew :backend:app:web:bootRun
```

빌드:
```powershell
.\gradlew :backend:app:web:build
.\gradlew :backend:app:worker:build
```

생성 결과:
- `backend/app/web/build/libs/web-1.0.0-SNAPSHOT.jar`
- `backend/app/worker/build/libs/worker-1.0.0-SNAPSHOT.jar`
- `backend/app/web/build/docs/asciidoc/index.html`

## DB/API Key
DB 접속 정보와 외부 API key는 환경변수로만 주입한다. 실제 값은 README나 커밋된 문서에 적지 않는다.

필수 환경변수:
- `ENJOYTRIP_DB_URL`
- `ENJOYTRIP_DB_USER`
- `ENJOYTRIP_DB_PASSWORD`
- `ENJOYTRIP_TOUR_API_KEY` 또는 `TOUR_API_KEY`
- `EV_CHARGER_API_KEY` 또는 `ENJOYTRIP_EV_API_KEY`

로컬 개발용 샘플은 `.env.example`을 복사한 뒤 개인 환경에 맞게 값을 채워 사용한다.
