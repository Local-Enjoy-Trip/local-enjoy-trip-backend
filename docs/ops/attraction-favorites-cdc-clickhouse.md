# 관광지 찜 CDC → Kafka Connect → ClickHouse 로컬 분석 파이프라인

## 범위

이 문서는 `attraction_favorites` 테이블 변경을 PostgreSQL logical replication, Debezium, Kafka Connect, ClickHouse로 전달하는 **phase 1 local-dev 전용** 분석 파이프라인을 설명한다.

명시적 비목표:

- `backend/web` read path 변경 없음
- 기존 favorite/unfavorite API 계약 변경 없음
- Java Kafka Streams 또는 별도 애플리케이션 프로세서 추가 없음
- production 배포/운영 전제 추가 없음

현재 executable module은 `backend/web`과 `backend/worker`로 분리되어 있으며, 둘 다 조립 목적의 `project(':backend:storage')` 의존성과 source-level storage 참조 금지 task를 함께 둔다. 이 문서는 attraction favorites CDC/ClickHouse 작업이 web read path를 불필요하게 넓히지 않는다는 의미로 유지한다.

## 구성 요소

`docker-compose.yml`의 CDC 관련 서비스는 다음과 같다.

| 서비스 | 역할 |
| --- | --- |
| `db` | PostGIS/PostgreSQL. `wal_level=logical`, replication slot/sender 제한을 로컬 CDC용으로 설정한다. |
| `kafka` | 단일 노드 KRaft Kafka broker. 내부 `kafka:29092`, 호스트 `localhost:9092`를 노출한다. |
| `kafka-connect` | Debezium PostgreSQL source와 ClickHouse sink plugin이 설치된 Kafka Connect worker. REST는 `localhost:8083`이다. |
| `clickhouse` | raw event table과 aggregate projection을 보관하는 local ClickHouse. HTTP/native 포트는 `8123`/`9000`이다. |
| `cdc-postgres-bootstrap` | Flyway 이후 `attraction_favorites_publication`을 생성/갱신하고 `REPLICA IDENTITY FULL`을 설정하는 one-shot 서비스. |
| `kafka-connect-bootstrap` | checked-in connector properties를 Kafka Connect REST API에 idempotent `PUT`으로 등록/갱신하는 one-shot 서비스. |

영속 volume은 로컬 replay/restart에 필요한 것만 둔다.

- `enjoytrip-postgres-data`
- `enjoytrip-kafka-data`
- `enjoytrip-clickhouse-data`
- 기존 `enjoytrip-pgadmin-data`

## Postgres bootstrap 순서

1. fresh Postgres volume에서 `infra/cdc/postgres/init/001_create_replication_user.sql`이 실행되어 `enjoytrip_cdc` replication user와 최소 SELECT 권한을 만든다.
2. backend Flyway가 `public.attraction_favorites`를 생성한다.
3. `infra/cdc/postgres/bootstrap-publication.sh`가 `infra/cdc/postgres/bootstrap-publication.sql`을 실행한다.
4. publication bootstrap은 다음을 보장한다.
   - `public.attraction_favorites` 존재 확인
   - delete 이벤트가 `attraction_id/user_id`를 포함하도록 `REPLICA IDENTITY FULL` 설정
   - `attraction_favorites_publication` 생성 또는 table membership 보정
   - `enjoytrip_cdc`에 table SELECT 권한 부여

주의: Docker의 `/docker-entrypoint-initdb.d`는 fresh data directory에서만 실행된다. 기존 `enjoytrip-postgres-data` volume이 있다면 다음 중 하나가 필요하다.

- 전체 local CDC volume 재생성
- 또는 `infra/cdc/postgres/init/001_create_replication_user.sql`을 기존 `db` 컨테이너에 수동 재실행

publication bootstrap은 Flyway 이후 언제든 재실행 가능하다.

## ClickHouse event model

Raw landing table: `attraction_favorites_events`

주요 컬럼:

- `event_id`: `source_lsn`, Kafka topic/partition/offset, `attraction_id`, `user_id`, `source_op` 기반 materialized deterministic audit key
- `attraction_id`, `user_id`
- `source_op`: Debezium operation. phase 1은 `c`, `r`, `d`만 허용한다.
- `is_deleted`: Debezium delete rewrite marker
- `source_lsn`, `source_ts_ms`, `tx_id`, `snapshot_flag`
- `kafka_topic`, `kafka_partition`, `kafka_offset`
- `ingested_at`
- `sign`: ClickHouse materialized delta. snapshot/create는 `+1`, delete는 `-1`이다.

정렬 키는 `(attraction_id, user_id, source_lsn, event_id)`이다.

Aggregate table: `attraction_favorites_counts`

- `SummingMergeTree` 기반으로 `attraction_id`별 `favorite_count` delta를 누적한다.
- 현재 count 조회는 `SELECT sum(favorite_count) ... GROUP BY attraction_id` 형태를 사용한다.

## Backend read path

`GET /api/attractions/popular-nearby`는 PostGIS가 먼저 반경 안의 주변 관광지 후보를 조회한 뒤, 후보 ID에 한해서
ClickHouse `attraction_favorites_counts`를 JDBC로 조회한다.

```sql
SELECT attraction_id, sum(favorite_count) AS favorite_count
FROM attraction_favorites_counts
WHERE attraction_id IN (...)
GROUP BY attraction_id;
```

- 기본 좌표는 서울 시청(`mapX=126.9780`, `mapY=37.5665`)이고 기본 반경은 500m다.
- 후보 중 하나라도 ClickHouse 집계 행이 있으면 `popularityCount` 내림차순, 거리, 제목/ID 순으로 정렬한다.
- 후보에 대한 ClickHouse 행이 없으면 PostGIS 주변 후보의 기본 거리순 결과를 그대로 반환한다.
- ClickHouse 연결 실패나 쿼리 실패도 API 실패로 전파하지 않고 warning log를 남긴 뒤 같은 PostGIS 기본 순서로 폴백한다.
- 기존 `favoriteCount`는 PostgreSQL 찜 수 의미를 유지하며, ClickHouse 집계 값은 `popularityCount`로만 노출한다.

업데이트 정책:

- phase 1에서 `attraction_favorites` update는 예상하지 않는다.
- `source_op = 'u'`는 raw table CHECK constraint에서 실패한다.
- DLQ/quarantine은 phase 1 범위가 아니며, update가 요구사항이 되는 별도 단계에서 설계한다.

## Connector configs

Checked-in connector 파일:

- `infra/cdc/connectors/debezium-attraction-favorites-source.properties`
- `infra/cdc/connectors/clickhouse-attraction-favorites-sink.properties`

Debezium source는 다음만 수행한다.

- `ExtractNewRecordState`로 Debezium envelope flatten
- `delete.tombstone.handling.mode=rewrite`로 delete row를 보존하고 tombstone 제거
- `op`, `table`, `lsn`, `source.ts_ms`, `source.txId`, `source.snapshot` metadata 추가

ClickHouse sink는 다음만 수행한다.

- Kafka topic/partition/offset audit field 삽입
- Debezium metadata field를 ClickHouse column 이름으로 rename
- `topic2TableMap`으로 `enjoytrip.public.attraction_favorites` → `attraction_favorites_events` 매핑
- bounded fetch micro-batching 설정
  - `consumer.override.max.poll.records=500`
  - `consumer.override.fetch.max.wait.ms=1000`
  - `consumer.override.fetch.min.bytes=1`
  - fetch byte cap 설정

Kafka Connect worker는 connector-level consumer override를 허용하기 위해 `CONNECT_CONNECTOR_CLIENT_CONFIG_OVERRIDE_POLICY=All`을 사용한다.

## 로컬 bring-up

### 1. 인프라 시작

```bash
docker compose up -d db kafka kafka-connect clickhouse redis pgadmin
```

처음 `kafka-connect` 이미지는 Debezium/ClickHouse connector를 설치하므로 build 시간이 걸릴 수 있다.

### 2. Flyway migration 실행

backend app을 로컬에서 실행해 기존 Flyway 경로로 `attraction_favorites`를 만든다.

```bash
./gradlew :backend:web:bootRun
```

이미 migration만 별도 수행하는 운영 절차가 있다면 그 절차를 사용해도 된다. phase 1은 backend를 compose service로 추가하지 않는다.

### 3. Postgres publication 및 connector 등록

Flyway 이후 one-shot bootstrap profile을 실행한다.

```bash
docker compose --profile cdc-bootstrap up cdc-postgres-bootstrap kafka-connect-bootstrap
```

재실행해도 connector config는 Kafka Connect REST API에 idempotent하게 reconcile된다.

### 4. 상태 확인

```bash
curl -s http://localhost:8083/connectors | jq
curl -s http://localhost:8083/connectors/attraction-favorites-postgres-source/status | jq
curl -s http://localhost:8083/connectors/attraction-favorites-clickhouse-sink/status | jq
```

## Smoke check

기존 API로 favorite/unfavorite를 호출해도 되고, 로컬 CDC 파이프라인 자체만 검증하려면 checked-in smoke script를 사용할 수 있다.

```bash
infra/cdc/connectors/smoke-attraction-favorites-cdc.sh
```

이 스크립트는 다음을 확인한다.

- 두 connector가 RUNNING인지 확인
- sample attraction에 `attraction_favorites` insert/delete 수행
- ClickHouse raw table에 create/delete event가 도착했는지 확인
- ClickHouse aggregate count와 Postgres count parity 확인
- `source_op = 'u'` raw row가 없는지 확인

## Kafka Connect restart 검증

routine restart는 offsets와 Postgres replication slot을 유지해야 한다.

```bash
docker compose restart kafka-connect
docker compose --profile cdc-bootstrap up kafka-connect-bootstrap
curl -s http://localhost:8083/connectors/attraction-favorites-postgres-source/status | jq
curl -s http://localhost:8083/connectors/attraction-favorites-clickhouse-sink/status | jq
```

이때 ClickHouse raw/aggregate table을 truncate하지 않는다.

## Manual reset / replay 정책

Routine connector restart와 manual reset은 다르다.

Routine restart:

- Kafka Connect offsets와 Postgres replication slot을 유지한다.
- bootstrap script만 재실행해 config drift를 reconcile한다.
- ClickHouse raw/aggregate table은 보존한다.

Manual full rebuild:

```bash
docker compose --profile cdc-bootstrap down -v
```

그 후 bring-up 순서를 처음부터 다시 수행한다.

Manual partial reset을 선택한다면 반드시 함께 수행해야 한다.

1. connector 중지/삭제
2. `infra/cdc/clickhouse/003_attraction_favorites_reset.sql`로 ClickHouse raw/aggregate truncate
3. Kafka Connect offset reset
4. Postgres replication slot reset
5. snapshot replay 전에 aggregate가 비어 있는지 확인

집계는 raw event에서 재계산 가능하지만, 비어 있지 않은 aggregate에 snapshot을 다시 흘려보내면 count가 중복될 수 있다. deterministic `event_id`는 audit/dedup 확인용이지, sink duplicate에 대한 correctness 보장이 아니다.

## 참고 문서

- Debezium PostgreSQL connector: https://debezium.io/documentation/reference/stable/connectors/postgresql.html
- Debezium New Record State Extraction SMT: https://debezium.io/documentation/reference/stable/transformations/event-flattening.html
- ClickHouse Kafka Connect Sink: https://github.com/ClickHouse/clickhouse-kafka-connect
