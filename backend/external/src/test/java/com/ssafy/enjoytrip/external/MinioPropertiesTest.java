package com.ssafy.enjoytrip.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.external.minio.MinioProperties;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class MinioPropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "enjoytrip.minio.endpoint=http://localhost:9000",
                    "enjoytrip.minio.bucket=dongnepin-notes",
                    "enjoytrip.minio.access-key=minioadmin",
                    "enjoytrip.minio.secret-key=minioadmin",
                    "enjoytrip.minio.region=ap-northeast-2",
                    "enjoytrip.minio.public-base-url=http://localhost:9000/dongnepin-notes",
                    "enjoytrip.minio.upload-expiry=15m"
            );

    @DisplayName("MinIO 설정은 enjoytrip.minio.* 값으로 바인딩된다")
    @Test
    void bindsMinioPropertiesFromEnjoytripPrefix() {
        contextRunner.run(context -> {
            MinioProperties properties = context.getBean(MinioProperties.class);

            assertThat(properties.getEndpoint()).isEqualTo("http://localhost:9000");
            assertThat(properties.getBucket()).isEqualTo("dongnepin-notes");
            assertThat(properties.getAccessKey()).isEqualTo("minioadmin");
            assertThat(properties.getSecretKey()).isEqualTo("minioadmin");
            assertThat(properties.getRegion()).isEqualTo("ap-northeast-2");
            assertThat(properties.getPublicBaseUrl()).isEqualTo("http://localhost:9000/dongnepin-notes");
            assertThat(properties.getUploadExpiry()).isEqualTo(Duration.ofMinutes(15));
        });
    }

    @Configuration
    @EnableConfigurationProperties(MinioProperties.class)
    static class TestConfig {
    }
}
