# Autopilot Ralplan: core-api/core-enum cleanup

## 목표
`core` 루트에 남은 구 단일 core 모듈 흔적을 제거하고, target 구조를 `core:core-api`, `core:core-enum`, `storage:db-core`로 고정한다. 기존 계층 분리 흔적은 이번 target에서는 유지 목표가 아니다. `core-api` 서비스는 `storage:db-core`의 entity/JPA/jOOQ를 직접 사용하고 별도 repository port를 만들지 않는다.

## 범위
1. 잘못 생성된 main checkout 변경은 되돌린다.
2. migration worktree `/Users/hj.park/projects/local-enjoy-trip-backend-monolithic-core-api-migration`에서 작업한다.
3. `settings.gradle`에서 루트 `core`, 루트 `storage`, legacy `external` compile path 의존을 정리한다.
4. `core/src` 및 `storage/src` 잔재는 target submodule로 이동 완료 여부를 검증한 뒤 제거한다.
5. `external`은 active outbound integration module로 분리하고, `support/auth`는 제거하며, `batch`는 `core-api`, `db-core`, `external` 기준으로 맞춘다.
6. `core-api` 내부의 core-domain repository 패키지는 제거하고 storage entity/JPA/jOOQ usage로 정리한다.

## 검증 기준
- `./gradlew :core:core-api:compileJava :storage:db-core:compileJava --console=plain`
- 가능하면 `./gradlew :core:core-api:check :storage:db-core:check --console=plain`
- `rg "project\(:core\)|project\(:storage\)|include core|include storage"`에서 legacy dependency/include 없음
- `find core -maxdepth 2 -type d` 결과가 `core-api`, `core-enum` 중심임

## stop rule
컴파일 실패가 storage repository/entity import 누락을 가리키면 그 import/path를 수정한다. 런타임 boot proof나 app 삭제는 별도 phase로 남기고 이번 단계에서 강제하지 않는다.
