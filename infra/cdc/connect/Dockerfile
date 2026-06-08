FROM confluentinc/cp-kafka-connect-base:7.8.0

ARG DEBEZIUM_POSTGRES_CONNECTOR_VERSION=3.2.6-2
ARG CLICKHOUSE_CONNECTOR_VERSION=v1.3.9

RUN confluent-hub install --no-prompt debezium/debezium-connector-postgresql:${DEBEZIUM_POSTGRES_CONNECTOR_VERSION} \
    && confluent-hub install --no-prompt clickhouse/clickhouse-kafka-connect:${CLICKHOUSE_CONNECTOR_VERSION}
