-- Idempotent post-Flyway publication bootstrap for CDC tables.
-- This must run after Flyway creates public.attraction_favorites and public.notification_outbox.

DO $$
BEGIN
    IF to_regclass('public.attraction_favorites') IS NULL THEN
        RAISE EXCEPTION 'public.attraction_favorites does not exist. Run backend Flyway migrations before CDC publication bootstrap.';
    END IF;
    IF to_regclass('public.notification_outbox') IS NULL THEN
        RAISE EXCEPTION 'public.notification_outbox does not exist. Run backend Flyway migrations before CDC publication bootstrap.';
    END IF;
END
$$;

-- Delete events must include attraction_id/user_id for ClickHouse count decrements.
ALTER TABLE public.attraction_favorites REPLICA IDENTITY FULL;

GRANT USAGE ON SCHEMA public TO :"cdc_user";
GRANT SELECT ON TABLE public.attraction_favorites TO :"cdc_user";
GRANT SELECT ON TABLE public.notification_outbox TO :"cdc_user";

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

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'notification_outbox_publication') THEN
        CREATE PUBLICATION notification_outbox_publication FOR TABLE public.notification_outbox;
    ELSE
        IF NOT EXISTS (
            SELECT 1
            FROM pg_publication p
            JOIN pg_publication_rel pr ON pr.prpubid = p.oid
            JOIN pg_class c ON c.oid = pr.prrelid
            JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE p.pubname = 'notification_outbox_publication'
              AND n.nspname = 'public'
              AND c.relname = 'notification_outbox'
        ) THEN
            ALTER PUBLICATION notification_outbox_publication ADD TABLE public.notification_outbox;
        END IF;
    END IF;
END
$$;
