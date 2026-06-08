CREATE TABLE IF NOT EXISTS attraction_favorites_counts
(
    attraction_id UInt64,
    favorite_count Int64
)
ENGINE = SummingMergeTree
ORDER BY attraction_id;

CREATE MATERIALIZED VIEW IF NOT EXISTS attraction_favorites_counts_mv
TO attraction_favorites_counts
AS
SELECT
    attraction_id,
    sign AS favorite_count
FROM attraction_favorites_events;
