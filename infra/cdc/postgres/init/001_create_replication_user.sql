-- Local-dev CDC bootstrap for Debezium.
-- Docker runs files in /docker-entrypoint-initdb.d only for a fresh Postgres data directory.
-- If enjoytrip-postgres-data already exists, rerun this file manually or recreate the volume.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'enjoytrip_cdc') THEN
        CREATE ROLE enjoytrip_cdc WITH LOGIN REPLICATION PASSWORD 'enjoytrip_cdc';
    ELSE
        ALTER ROLE enjoytrip_cdc WITH LOGIN REPLICATION PASSWORD 'enjoytrip_cdc';
    END IF;
END
$$;

GRANT CONNECT ON DATABASE enjoytrip TO enjoytrip_cdc;
GRANT USAGE ON SCHEMA public TO enjoytrip_cdc;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO enjoytrip_cdc;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO enjoytrip_cdc;
