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
    @DisplayName("core-api 설정은 storage MyBatis 설정을 명시적으로 import한다")
    void applicationImportsStorageMyBatisConfiguration() throws Exception {
        Resource application = resolver.getResource("classpath:application.yml");

        String content = application.getContentAsString(StandardCharsets.UTF_8);

        assertThat(content).contains("classpath:application-storage.yml");
    }

    @Test
    @DisplayName("storage MyBatis mapper XML은 core-api 테스트 클래스패스에서 발견된다")
    void storageMyBatisMappersAreDiscoverable() throws Exception {
        Resource[] mapperResources = resolver.getResources("classpath*:mybatis/mapper/**/*.xml");

        assertThat(mapperResources)
                .extracting(Resource::getFilename)
                .contains("AttractionMapper.xml", "MemberMapper.xml", "NotificationMapper.xml");
    }
}
