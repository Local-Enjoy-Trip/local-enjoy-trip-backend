package com.ssafy.enjoytrip.storage.integration;

import com.ssafy.enjoytrip.storage.StorageConfiguration;
import com.ssafy.enjoytrip.storage.testsupport.PostgisTestcontainersConfiguration;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;

@ActiveProfiles("postgis")
@Tag("postgis")
@SpringBootTest(classes = PostgisConnectionTest.TestApplication.class)
class PostgisConnectionTest {
    @Autowired
    private DSLContext dslContext;

    @Test
    void connectsToPostgisContainer() {
        String version = dslContext.select(field("PostGIS_Version()", String.class))
                .fetchOne(0, String.class);

        assertThat(version).isNotBlank();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({StorageConfiguration.class, PostgisTestcontainersConfiguration.class})
    static class TestApplication {
    }
}
