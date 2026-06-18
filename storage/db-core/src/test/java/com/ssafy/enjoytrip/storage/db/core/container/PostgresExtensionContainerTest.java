package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostgresExtensionContainerTest extends StorageContainerTestSupport {
    @DisplayName("ServiceConnection으로 연결된 PostgreSQL은 PostGIS와 pgvector extension을 제공한다")
    @Test
    void serviceConnectionPostgresSupportsPostgisAndPgvectorTogether() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet spatial = statement.executeQuery("""
                    select ST_DWithin(
                        ST_SetSRID(ST_MakePoint(126.9780, 37.5665), 4326)::geography,
                        ST_SetSRID(ST_MakePoint(126.9781, 37.5666), 4326)::geography,
                        50
                    ) as nearby
                    """)) {
                assertThat(spatial.next()).isTrue();
                assertThat(spatial.getBoolean("nearby")).isTrue();
            }

            try (ResultSet vector = statement.executeQuery(
                    "select vector_dims('[1,2,3]'::vector) as dimensions"
            )) {
                assertThat(vector.next()).isTrue();
                assertThat(vector.getInt("dimensions")).isEqualTo(3);
            }
        }
    }
}
