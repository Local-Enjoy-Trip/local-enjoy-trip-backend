package com.ssafy.enjoytrip.storage.db.core.container;

import com.ssafy.enjoytrip.storage.db.core.StorageConfiguration;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Tag("container")
@Tag("slow")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(
        classes = StorageContainerTestSupport.TestApplication.class,
        properties = {
                "mybatis.mapper-locations=classpath*:mybatis/mapper/**/*.xml",
                "mybatis.type-aliases-package=com.ssafy.enjoytrip.storage.db.core.model"
        }
)
abstract class StorageContainerTestSupport {
    private static final DockerImageName IMAGE = DockerImageName
            .parse("enjoytrip-postgis-pgvector:17-3.5")
            .asCompatibleSubstituteFor("postgres");

    @Container
    @ServiceConnection(name = "postgres")
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(IMAGE)
            .withDatabaseName("enjoytrip")
            .withUsername("enjoytrip")
            .withPassword("enjoytrip");

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected String uniqueId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    protected Long seedMember(String name, String email) {
        jdbcTemplate.update(
                """
                insert into members (name, email, password, created_at)
                values (?, ?, ?, current_timestamp)
                on conflict (email) do nothing
                """,
                name,
                email,
                "encoded-password"
        );

        return jdbcTemplate.queryForObject(
                "select id from members where email = ?",
                Long.class,
                email
        );
    }

    protected void seedAttraction(long id, String title, int sidoCode, int gugunCode) {
        jdbcTemplate.update("""
                insert into attractions (
                    id,
                    title,
                    addr1,
                    addr2,
                    read_count,
                    sido_code,
                    gugun_code,
                    content_type_id,
                    overview,
                    location
                )
                values (?, ?, ?, ?, 10, ?, ?, '12', ?, ST_SetSRID(ST_MakePoint(126.9780, 37.5665), 4326))
                on conflict (id) do nothing
                """, id, title, "서울 중구", "역삼동", sidoCode, gugunCode, title + " overview");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(StorageConfiguration.class)
    static class TestApplication {
    }
}
