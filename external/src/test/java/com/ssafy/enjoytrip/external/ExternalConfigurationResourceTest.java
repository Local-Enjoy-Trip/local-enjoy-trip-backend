package com.ssafy.enjoytrip.external;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

class ExternalConfigurationResourceTest {
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Test
    @DisplayName("external.yml은 API-facing external 클라이언트 설정을 소유한다")
    void externalConfigurationOwnsExternalClientProperties() throws Exception {
        String content = resolver.getResource("classpath:external.yml")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(content)
                .contains("spring:\n  ai:")
                .contains("https://gms.ssafy.io/gmsapi/api.openai.com/v1")
                .contains("enjoytrip:\n  ai:")
                .contains("kma:")
                .contains("minio:")
                .doesNotContain("optional:file:")
                .doesNotContain(".env");
    }
}
