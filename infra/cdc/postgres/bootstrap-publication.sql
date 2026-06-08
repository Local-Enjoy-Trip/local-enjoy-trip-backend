-- Idempotent post-Flyway publication bootstrap for phase-1 attraction_favorites CDC.
-- This must run after Flyway creates public.attraction_favorites.

DO $$
BEGIN
    IF to_regclass('public.attraction_favorites') IS NULL THEN
        RAISE EXCEPTION 'public.attraction_favorites does not exist. Run backend Flyway migrations before CDC publication bootstrap.';
    END IF;
END
$$;

-- Delete events must include attraction_id/user_id for ClickHouse count decrements.
ALTER TABLE public.attraction_favorites REPLICA IDENTITY FULL;

GRANT USAGE ON SCHEMA public TO enjoytrip_cdc;
GRANT SELECT ON TABLE public.attraction_favorites TO enjoytrip_cdc;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'attraction_favorites_publication') THEN
        CREATE PUBLICATION attraction_favorites_publication FOR TABLE public.attraction_favorites;
    ELSE
        IF NOT EXISTS (
            SELECT 1
            FROM pg_publication p
            JOIN pg_publication_rel pr ON pr.prpubid = p.oid
            JOIN pg_class c ON c.oid = pr.prrelid
            JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE p.pubname = 'attraction_favorites_publication'
              AND n.nspname = 'public'
              AND c.relname = 'attraction_favorites'
        ) THEN
            ALTER PUBLICATION attraction_favorites_publication ADD TABLE public.attraction_favorites;
        END IF;
    END IF;
END
$$;
