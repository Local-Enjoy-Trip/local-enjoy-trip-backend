# EnjoyTrip Backend (Spring Boot)

## 구조
- `backend/app`: Spring Boot 진입점, REST controller, application service, REST Docs 테스트
- `backend/core`: domain model, repository interface 같은 application contract
- `backend/storage`: JPA entity, Spring Data repository, JPA-backed adapter, DB model, PostgreSQL/PostGIS schema 관리
- `backend/external`: 한국관광공사 Tour API, EV 충전소 API, 뉴스 RSS client

`backend/app`에서는 `com.ssafy.enjoytrip.storage.*`를 import하면 안 된다. 저장소 구현은 전부 `backend/storage`에 두고, `backend:app:check`의 `forbidStorageReferences`가 app의 storage import와 compile classpath 의존성을 차단한다.

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
- `POST /api/plans?action=create|delete`
- `GET /api/boards`
- `POST /api/boards?action=create|update|delete`

RESTful 신규 mutation 경로도 함께 제공한다:
- `POST /api/members/signup`, `POST /api/members/login`, `PUT /api/members/{userId}`, `DELETE /api/members/{userId}`
- `POST /api/notices/items`, `PUT /api/notices/{id}`, `DELETE /api/notices/{id}`
- `POST /api/boards/posts`, `PUT /api/boards/{id}`, `DELETE /api/boards/{id}`
- `POST /api/hotplaces/items`, `DELETE /api/hotplaces/{id}`
- `POST /api/plans/items`, `DELETE /api/plans/{id}`

## 실행
```powershell
.\gradlew :backend:app:bootRun
```

빌드:
```powershell
.\gradlew :backend:app:build
```

생성 결과:
- `backend/app/build/libs/app-1.0.0-SNAPSHOT.jar`
- `backend/app/build/docs/asciidoc/index.html`

## DB/API Key
기본 DB 값:
- URL: `jdbc:postgresql://localhost:5432/enjoytrip`
- USER: `ssafy`
- PASSWORD: `ssafy`

환경변수:
- `ENJOYTRIP_DB_URL`
- `ENJOYTRIP_DB_USER`
- `ENJOYTRIP_DB_PASSWORD`
- `ENJOYTRIP_TOUR_API_KEY` 또는 `TOUR_API_KEY`
- `EV_CHARGER_API_KEY` 또는 `ENJOYTRIP_EV_API_KEY`

## AI Redis Stream -> SSE delivery contract

The AI handoff uses Redis Stream as a low-latency live fan-out channel for authenticated SSE clients.

- For HTTP AI requests that include `client_id`, publishing to Redis Stream is part of the HTTP success path: if the AI server cannot hand the response to Redis after bounded retries, the HTTP call fails with `503` rather than silently losing the backend notification.
- After Redis accepts the event, SSE delivery is best-effort live notification, not durable end-user delivery confirmation.
- Backend reads the stream per application instance rather than with a competing consumer group, so every instance can deliver events to its own local SSE connections.
- `/api/ai/events` requires the app JWT; the SSE subscription key is the JWT subject (`userId`).
- AI HTTP requests should carry `client_id` and `request_id` when the caller expects an SSE event. Targetless events are not published for SSE and backend targetless events are dropped instead of broadcast to avoid leaking private AI responses.
- The current contract does not provide disconnected-client replay. Redis `MAXLEN` retention only bounds stream memory for live fan-out.
