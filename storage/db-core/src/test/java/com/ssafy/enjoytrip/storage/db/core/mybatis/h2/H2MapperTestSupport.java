package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import com.ssafy.enjoytrip.storage.db.core.StorageConfiguration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(
        classes = H2MapperTestSupport.TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:dbcore;MODE=PostgreSQL;"
                        + "DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;NON_KEYWORDS=DAY",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.flyway.enabled=false",
                "mybatis.mapper-locations=classpath*:mybatis/mapper/**/*.xml",
                "mybatis.type-aliases-package=com.ssafy.enjoytrip.storage.db.core.model"
        }
)
@Sql(scripts = "/h2/mapper-schema.sql")
abstract class H2MapperTestSupport {
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearStorageRows() {
        jdbcTemplate.update("delete from notifications");
        jdbcTemplate.update("delete from attraction_popularity_stats");
        jdbcTemplate.update("delete from attraction_saves");
        jdbcTemplate.update("delete from attraction_ratings");
        jdbcTemplate.update("delete from course_tags");
        jdbcTemplate.update("delete from tags");
        jdbcTemplate.update("delete from attractions");
        jdbcTemplate.update("delete from friendships");
        jdbcTemplate.update("delete from course_saves");
        jdbcTemplate.update("delete from course_items");
        jdbcTemplate.update("delete from courses");
        jdbcTemplate.update("delete from notes");
        jdbcTemplate.update("delete from plan_items");
        jdbcTemplate.update("delete from plans");
        jdbcTemplate.update("delete from news_items");
        jdbcTemplate.update("delete from hotplaces");
        jdbcTemplate.update("delete from notices");
        jdbcTemplate.update("delete from boards");
        jdbcTemplate.update("delete from auth_logs");
        jdbcTemplate.update("delete from members");
    }

    protected String uniqueId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    protected Long seedMember(String name, String email) {
        jdbcTemplate.update(
                """
                insert into members (name, email, password, created_at)
                values (?, ?, ?, current_timestamp)
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

    protected void seedAttraction(Long attractionId, String title) {
        jdbcTemplate.update("""
                insert into attractions (id, title, status, created_at)
                values (?, ?, 'ACTIVE', current_timestamp)
                """, attractionId, title);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(StorageConfiguration.class)
    static class TestApplication {
    }
}
