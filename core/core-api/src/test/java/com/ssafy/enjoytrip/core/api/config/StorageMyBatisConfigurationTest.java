package com.ssafy.enjoytrip.core.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class StorageMyBatisConfigurationTest {
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Test
    @DisplayName("core-api application.yml은 .env 파일을 직접 import하지 않는다")
    void applicationDoesNotImportDotEnvFiles() throws Exception {
        String content = resourceContent("classpath:application.yml");

        assertThat(content).doesNotContain("optional:file:", ".env");
    }

    @Test
    @DisplayName("core-api 설정은 storage DB/MyBatis 설정을 db-core.yml로 import한다")
    void applicationImportsStorageMyBatisConfiguration() throws Exception {
        String content = resourceContent("classpath:application.yml");

        assertThat(content)
                .contains("db-core.yml")
                .doesNotContain("spring:\n  datasource:")
                .doesNotContain("classpath:application-storage.yml");
    }

    @Test
    @DisplayName("core-api 설정은 external 클라이언트 설정을 external.yml로 import한다")
    void applicationImportsExternalConfiguration() throws Exception {
        String content = resourceContent("classpath:application.yml");
        String externalContent = resourceContent("external.yml");

        assertThat(content)
                .contains("external.yml")
                .doesNotContain("open-weather-map")
                .doesNotContain("spring:\n  ai:")
                .doesNotContain("enjoytrip:\n  ai:")
                .doesNotContain("minio:");
        assertThat(externalContent)
                .contains("kma")
                .contains("spring:\n  ai:")
                .contains("minio:");
    }

    @Test
    @DisplayName("core-api 설정은 support monitoring 설정을 classpath 리소스로 import한다")
    void applicationImportsSupportMonitoringConfiguration() throws Exception {
        String content = resourceContent("classpath:application.yml");

        assertThat(content).contains("monitoring.yml");
        assertThat(resolver.getResource("monitoring.yml").exists()).isTrue();
    }

    @Test
    @DisplayName("core-api 런타임은 support logging 리소스를 classpath에서 사용한다")
    void supportLoggingResourceIsDiscoverable() {
        assertThat(resolver.getResource("classpath:logback-spring.xml").exists()).isTrue();
    }

    @Test
    @DisplayName("storage MyBatis mapper XML은 core-api 테스트 클래스패스에서 발견된다")
    void storageMyBatisMappersAreDiscoverable() throws Exception {
        Resource[] mapperResources = resolver.getResources("classpath*:mybatis/mapper/**/*.xml");

        assertThat(mapperResources)
                .extracting(Resource::getFilename)
                .contains("AttractionMapper.xml", "MemberMapper.xml", "NotificationMapper.xml");
    }

    private String resourceContent(String location) throws Exception {
        return resolver.getResource(location).getContentAsString(StandardCharsets.UTF_8);
    }
}
