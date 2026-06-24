package com.ssafy.enjoytrip.batch;

import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingTargetRegion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmbeddingTargetRegionsTest {
    @DisplayName("대상 지역 YAML은 증빙이 있는 서울만 포함한다")
    @Test
    void targetRegionYamlContainsExactlySeoulWithProof() {
        Properties properties = yamlProperties();

        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[0].sido-name")).isEqualTo("서울특별시");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[0].gugun-name")).isEqualTo("전체");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[0].sido-code")).isEqualTo("1");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[0].gugun-code")).isEqualTo("0");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[0].provenance")).contains("서울");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[1].sido-name")).isNull();
    }

    @DisplayName("검증기는 표준 지역 쌍 밖의 지역을 거부한다")
    @Test
    void validatorRejectsAnyRegionOutsideCanonicalPair() {
        AttractionEmbeddingTargetRegionValidator validator = new AttractionEmbeddingTargetRegionValidator();
        List<AttractionEmbeddingTargetRegion> invalid = List.of(
                new AttractionEmbeddingTargetRegion("서울특별시", "전체", 1, 0, "proof"),
                new AttractionEmbeddingTargetRegion("강원특별자치도", "강릉시", 32, 1, "proof")
        );

        assertThatThrownBy(() -> validator.validate(invalid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("서울만 포함해야 합니다.");
    }

    private static Properties yamlProperties() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("embedding-target-regions.yml"));
        Properties properties = factory.getObject();
        assertThat(properties).isNotNull();
        return properties;
    }
}
