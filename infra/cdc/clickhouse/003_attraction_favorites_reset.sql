-- Manual local reset helper. Run only when connector offsets/slot are reset or local CDC volumes are rebuilt.
-- Do not run this for a routine Kafka Connect restart.
TRUNCATE TABLE IF EXISTS attraction_favorites_counts;
TRUNCATE TABLE IF EXISTS attraction_favorites_events;
