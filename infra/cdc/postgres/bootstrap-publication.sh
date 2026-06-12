#!/usr/bin/env bash
set -euo pipefail

: "${POSTGRES_HOST:=db}"
: "${POSTGRES_PORT:=5432}"
: "${POSTGRES_DB:=enjoytrip}"
: "${POSTGRES_USER:=ssafy}"
: "${POSTGRES_PASSWORD:=ssafy}"
: "${CDC_DB_USER:=enjoytrip_cdc}"
: "${CDC_DB_PASSWORD:=enjoytrip_cdc}"
: "${CDC_PUBLICATION_BOOTSTRAP_TIMEOUT_SECONDS:=600}"

export PGPASSWORD="$POSTGRES_PASSWORD"
SQL_FILE="${CDC_PUBLICATION_SQL:-/cdc/postgres/bootstrap-publication.sql}"

deadline=$((SECONDS + CDC_PUBLICATION_BOOTSTRAP_TIMEOUT_SECONDS))

printf "[cdc-postgres-bootstrap] waiting for public.attraction_favorites in %s:%s/%s\n" "$POSTGRES_HOST" "$POSTGRES_PORT" "$POSTGRES_DB"
while true; do
  if psql -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc "select to_regclass('public.attraction_favorites') is not null" 2>/dev/null | grep -qx t; then
    break
  fi

  if (( SECONDS >= deadline )); then
    echo "[cdc-postgres-bootstrap] attraction_favorites table was not found before timeout. Run :backend:web:bootRun or Flyway migrations, then rerun this service." >&2
    exit 1
  fi

  sleep 5
done

psql -v ON_ERROR_STOP=1 \
  -v cdc_user="$CDC_DB_USER" \
  -v cdc_password="$CDC_DB_PASSWORD" \
  -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<'SQL'
SELECT format('CREATE ROLE %I WITH LOGIN REPLICATION PASSWORD %L', :'cdc_user', :'cdc_password')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'cdc_user')
\gexec

SELECT format('ALTER ROLE %I WITH LOGIN REPLICATION PASSWORD %L', :'cdc_user', :'cdc_password')
WHERE EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'cdc_user')
\gexec

SELECT format('GRANT CONNECT ON DATABASE %I TO %I', current_database(), :'cdc_user')
\gexec
SQL

psql -v ON_ERROR_STOP=1 \
  -v cdc_user="$CDC_DB_USER" \
  -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -f "$SQL_FILE"
psql -v ON_ERROR_STOP=1 -h "$POSTGRES_HOST" -p "$POSTGRES_PORT" -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<'SQL'
select name, setting from pg_settings where name in ('wal_level', 'max_replication_slots', 'max_wal_senders') order by name;
select pubname from pg_publication where pubname = 'attraction_favorites_publication';
select schemaname, tablename from pg_publication_tables where pubname = 'attraction_favorites_publication';
select relreplident from pg_class where oid = 'public.attraction_favorites'::regclass;
SQL

echo "[cdc-postgres-bootstrap] publication is ready"
