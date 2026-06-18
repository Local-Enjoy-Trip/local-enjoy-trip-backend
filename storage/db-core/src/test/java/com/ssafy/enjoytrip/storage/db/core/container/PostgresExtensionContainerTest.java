package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.StorageConfiguration;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteMapPinRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoteRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoteMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Tag("container")
@Tag("postgis")
@Tag("pgvector")
@Tag("slow")
@Testcontainers
@SpringBootTest(
        classes = PostgresExtensionContainerTest.TestApplication.class,
        properties = {
                "mybatis.mapper-locations=classpath*:mybatis/mapper/**/*.xml",
                "mybatis.type-aliases-package=com.ssafy.enjoytrip.storage.db.core.model"
        }
)
class PostgresExtensionContainerTest {
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
    private DataSource dataSource;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private NoteMapper noteMapper;

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

    @DisplayName("Flyway로 생성된 members 테이블에 MyBatis mapper가 insert/select/update를 수행한다")
    @Test
    void memberMapperWorksAgainstMigratedPostgres() {
        MemberRecord record = new MemberRecord(
                "member-service-connection",
                "서비스커넥션",
                null,
                "service-connection@example.com",
                "encoded-password",
                null,
                37.5665,
                126.9780,
                "서울 중구"
        );

        memberMapper.insert(record);
        MemberRecord saved = memberMapper.findByUserId("member-service-connection");
        saved.update(
                "서비스커넥션수정",
                "sc",
                "service-connection-updated@example.com",
                "encoded-password-2",
                null,
                37.5700,
                126.9820,
                "서울 종로구"
        );
        memberMapper.update(saved);

        MemberRecord updated = memberMapper.findByEmail("service-connection-updated@example.com");

        assertThat(updated.getId()).isNotNull();
        assertThat(updated.getName()).isEqualTo("서비스커넥션수정");
        assertThat(updated.getNickname()).isEqualTo("sc");
        assertThat(updated.getRepresentativeLatitude()).isEqualTo(37.5700);
        assertThat(updated.getRepresentativeLongitude()).isEqualTo(126.9820);
        assertThat(updated.getCreatedAt()).isNotNull();
    }

    @DisplayName("Note mapper는 migration schema에서 insert/nearby/map-pin/update/delete SQL을 실행한다")
    @Test
    void noteMapperWorksAgainstMigratedPostgisSchema() {
        memberMapper.insert(new MemberRecord(
                "note-author",
                "노트작성자",
                "note-writer",
                "note-author@example.com",
                "encoded-password",
                "https://example.com/profile.png",
                37.5665,
                126.9780,
                "서울 중구"
        ));
        NoteRecord saved = noteMapper.insert(new NoteRecord(
                "note-author",
                "서비스커넥션 노트",
                "실제 PostgreSQL에서 저장되는 노트",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"),
                "서울 중구",
                "notes/service-connection.png",
                "https://example.com/notes/service-connection.png",
                "image/png"
        ));

        List<NoteRecord> nearby = noteMapper.findNearbyAccessible(
                126.9781,
                37.5666,
                100,
                10,
                null
        );
        List<NoteMapPinRecord> pins = noteMapper.findMapPins(
                126.9781,
                37.5666,
                100,
                10,
                null,
                "TIP",
                false
        );
        NoteRecord updated = noteMapper.updateOwned(new NoteRecord(
                saved.getId(),
                "note-author",
                "서비스커넥션 노트 수정",
                "실제 PostgreSQL에서 수정되는 노트",
                "TIP",
                "PUBLIC",
                new BigDecimal("37.5667000"),
                new BigDecimal("126.9782000"),
                "서울 중구",
                "notes/service-connection-updated.png",
                "https://example.com/notes/service-connection-updated.png",
                "image/png"
        ));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(nearby).extracting(NoteRecord::getId).contains(saved.getId());
        assertThat(pins).extracting(NoteMapPinRecord::id).contains(saved.getId());
        assertThat(updated.getTitle()).isEqualTo("서비스커넥션 노트 수정");
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(noteMapper.softDeleteOwned(saved.getId(), "note-author")).isEqualTo(1);
        assertThat(noteMapper.findById(saved.getId()).getDeletedAt()).isNotNull();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(StorageConfiguration.class)
    static class TestApplication {
    }
}
