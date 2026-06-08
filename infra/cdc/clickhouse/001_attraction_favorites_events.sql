CREATE TABLE IF NOT EXISTS attraction_favorites_events
(
    attraction_id UInt64,
    user_id String,
    source_op LowCardinality(String),
    is_deleted String DEFAULT 'false',
    source_lsn UInt64,
    source_ts_ms Nullable(Int64),
    tx_id Nullable(UInt64),
    snapshot_flag String DEFAULT 'false',
    source_table LowCardinality(String) DEFAULT 'attraction_favorites',
    kafka_topic String DEFAULT '',
    kafka_partition Int32 DEFAULT -1,
    kafka_offset Int64 DEFAULT -1,
    ingested_at DateTime64(3) DEFAULT now64(3),
    event_id String MATERIALIZED lower(hex(MD5(concat(
        toString(source_lsn), ':',
        kafka_topic, ':',
        toString(kafka_partition), ':',
        toString(kafka_offset), ':',
        toString(attraction_id), ':',
        user_id, ':',
        source_op
    )))),
    sign Int8 MATERIALIZED multiIf(
        source_op = 'd' OR lower(is_deleted) IN ('true', '1'), -1,
        source_op IN ('c', 'r'), 1,
        0
    ),
    CONSTRAINT chk_attraction_favorites_source_op CHECK source_op IN ('c', 'r', 'd'),
    CONSTRAINT chk_attraction_favorites_delete_consistency CHECK (source_op = 'd') OR lower(is_deleted) NOT IN ('true', '1')
)
ENGINE = MergeTree
ORDER BY (attraction_id, user_id, source_lsn, event_id);
