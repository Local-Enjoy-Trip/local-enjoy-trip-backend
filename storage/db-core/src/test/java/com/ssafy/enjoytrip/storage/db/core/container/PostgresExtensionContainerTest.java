package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Tag("container")
@Tag("postgis")
@Tag("pgvector")
@Tag("slow")
@Testcontainers
class PostgresExtensionContainerTest {
    private static final DockerImageName IMAGE = DockerImageName
            .parse("enjoytrip-postgis-pgvector:17-3.5")
            .asCompatibleSubstituteFor("postgres");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(IMAGE)
            .withDatabaseName("enjoytrip")
            .withUsername("enjoytrip")
            .withPassword("enjoytrip");

    @DisplayName("단일 PostgreSQL container image는 PostGIS와 pgvector extension을 함께 제공한다")
    @Test
    void supportsPostgisAndPgvectorTogether() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );
             Statement statement = connection.createStatement()) {
            statement.execute("create extension if not exists postgis");
            statement.execute("create extension if not exists vector");
            statement.execute("create table spatial_items (id bigint primary key, location geometry(Point, 4326))");
            statement.execute("insert into spatial_items values (1, ST_SetSRID(ST_MakePoint(126.9780, 37.5665), 4326))");
            statement.execute("create table vector_items (id bigint primary key, embedding vector(3))");
            statement.execute("insert into vector_items values (1, '[1,2,3]'::vector)");

            try (ResultSet spatial = statement.executeQuery("""
                    select ST_DWithin(
                        location::geography,
                        ST_SetSRID(ST_MakePoint(126.9781, 37.5666), 4326)::geography,
                        50
                    ) as nearby
                    from spatial_items
                    """)) {
                assertThat(spatial.next()).isTrue();
                assertThat(spatial.getBoolean("nearby")).isTrue();
            }

            try (ResultSet vector = statement.executeQuery("""
                    select vector_dims(embedding) as dimensions
                    from vector_items
                    """)) {
                assertThat(vector.next()).isTrue();
                assertThat(vector.getInt("dimensions")).isEqualTo(3);
            }
        }
    }
}
