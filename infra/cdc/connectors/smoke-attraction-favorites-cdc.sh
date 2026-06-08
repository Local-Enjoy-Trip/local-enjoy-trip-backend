#!/usr/bin/env bash
set -euo pipefail

: "${POSTGRES_DB:=enjoytrip}"
: "${POSTGRES_USER:=ssafy}"
: "${CLICKHOUSE_PASSWORD:=enjoytrip_clickhouse}"
: "${KAFKA_CONNECT_URL:=http://localhost:8083}"
: "${CDC_SMOKE_TIMEOUT_SECONDS:=120}"

pg_query() {
  docker compose exec -T db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "$1"
}

ch_query() {
  docker compose exec -T clickhouse clickhouse-client --user default --password "$CLICKHOUSE_PASSWORD" --query "$1"
}

connect_status() {
  python3 - "$KAFKA_CONNECT_URL" <<'PYCONNECT'
import json
import sys
import urllib.request
base = sys.argv[1].rstrip('/')
for name in ('attraction-favorites-postgres-source', 'attraction-favorites-clickhouse-sink'):
    with urllib.request.urlopen(f'{base}/connectors/{name}/status', timeout=10) as response:
        status = json.loads(response.read().decode())
    connector_state = status['connector']['state']
    task_states = [task['state'] for task in status.get('tasks', [])]
    print(f'{name}: connector={connector_state}, tasks={task_states}')
    if connector_state != 'RUNNING' or any(state != 'RUNNING' for state in task_states):
        raise SystemExit(f'{name} is not RUNNING')
PYCONNECT
}

wait_until() {
  local description="$1"
  local command="$2"
  local deadline=$((SECONDS + CDC_SMOKE_TIMEOUT_SECONDS))
  until eval "$command"; do
    if (( SECONDS >= deadline )); then
      echo "[cdc-smoke] timeout waiting for ${description}" >&2
      return 1
    fi
    sleep 2
  done
}

sql_escape() {
  printf "%s" "$1" | sed "s/'/''/g"
}

cleanup() {
  if [[ -n "${escaped_user_id:-}" && -n "${attraction_id:-}" ]]; then
    pg_query "DELETE FROM attraction_favorites WHERE attraction_id = ${attraction_id} AND user_id = '${escaped_user_id}'" >/dev/null 2>&1 || true
  fi
  if [[ "${seeded_attraction:-0}" == "1" && -n "${attraction_id:-}" ]]; then
    pg_query "DELETE FROM attractions WHERE id = ${attraction_id}" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

connect_status

update_count=$(ch_query "SELECT count() FROM attraction_favorites_events WHERE source_op = 'u'")
if [[ "$update_count" != "0" ]]; then
  echo "[cdc-smoke] unexpected update CDC rows found: ${update_count}" >&2
  exit 1
fi

seeded_attraction=0
attraction_id=$(pg_query "SELECT id FROM attractions ORDER BY id LIMIT 1")
if [[ -z "$attraction_id" ]]; then
  attraction_id=987654321
  seeded_attraction=1
  pg_query "INSERT INTO attractions (id, title, addr1, read_count, location) VALUES (${attraction_id}, 'CDC Smoke Attraction', 'local smoke', 0, ST_SetSRID(ST_MakePoint(127.0, 37.0), 4326)) ON CONFLICT (id) DO NOTHING"
  echo "[cdc-smoke] seeded synthetic attraction_id=${attraction_id}"
fi

user_id="cdc-smoke-$(date +%s)-$$"
escaped_user_id=$(sql_escape "$user_id")

echo "[cdc-smoke] attraction_id=${attraction_id}, user_id=${user_id}"
pg_query "DELETE FROM attraction_favorites WHERE attraction_id = ${attraction_id} AND user_id = '${escaped_user_id}'"
pg_query "INSERT INTO attraction_favorites (attraction_id, user_id) VALUES (${attraction_id}, '${escaped_user_id}') ON CONFLICT (attraction_id, user_id) DO NOTHING"

wait_until "insert CDC row" "[[ \$(ch_query \"SELECT count() FROM attraction_favorites_events WHERE attraction_id = ${attraction_id} AND user_id = '${escaped_user_id}' AND source_op IN ('c', 'r')\") != 0 ]]"

wait_until "ClickHouse/Postgres count parity after insert" "[[ \$(ch_query \"SELECT toString(coalesce(sum(favorite_count), 0)) FROM attraction_favorites_counts WHERE attraction_id = ${attraction_id}\") == \$(pg_query \"SELECT count(*) FROM attraction_favorites WHERE attraction_id = ${attraction_id}\") ]]"

pg_query "DELETE FROM attraction_favorites WHERE attraction_id = ${attraction_id} AND user_id = '${escaped_user_id}'"
wait_until "delete CDC row" "[[ \$(ch_query \"SELECT count() FROM attraction_favorites_events WHERE attraction_id = ${attraction_id} AND user_id = '${escaped_user_id}' AND source_op = 'd'\") != 0 ]]"
wait_until "ClickHouse/Postgres count parity after delete" "[[ \$(ch_query \"SELECT toString(coalesce(sum(favorite_count), 0)) FROM attraction_favorites_counts WHERE attraction_id = ${attraction_id}\") == \$(pg_query \"SELECT count(*) FROM attraction_favorites WHERE attraction_id = ${attraction_id}\") ]]"

update_count=$(ch_query "SELECT count() FROM attraction_favorites_events WHERE source_op = 'u'")
if [[ "$update_count" != "0" ]]; then
  echo "[cdc-smoke] unexpected update CDC rows found after smoke: ${update_count}" >&2
  exit 1
fi

echo "[cdc-smoke] insert/delete CDC and aggregate parity passed"
