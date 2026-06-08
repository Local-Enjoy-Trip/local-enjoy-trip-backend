package com.ssafy.enjoytrip.batch;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingTargetRegion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmbeddingTargetRegionsTest {
    @Test
    void targetRegionYamlContainsExactlyGangneungAndJeonjuWithProof() {
        Properties properties = yamlProperties();

        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[0].sido-name")).isEqualTo("강원특별자치도");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[0].gugun-name")).isEqualTo("강릉시");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[0].sido-code")).isEqualTo("32");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[0].gugun-code")).isEqualTo("1");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[0].provenance")).contains("TourAPI");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[1].sido-name")).isEqualTo("전북특별자치도");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[1].gugun-name")).isEqualTo("전주시");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[1].sido-code")).isEqualTo("37");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[1].gugun-code")).isEqualTo("12");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[1].provenance")).contains("TourAPI");
        assertThat(properties.getProperty("enjoytrip.ai.embedding.target-regions.regions[2].sido-name")).isNull();
    }

    @Test
    void validatorRejectsAnyRegionOutsideCanonicalPair() {
        AttractionEmbeddingTargetRegionValidator validator = new AttractionEmbeddingTargetRegionValidator();
        List<AttractionEmbeddingTargetRegion> invalid = List.of(
                new AttractionEmbeddingTargetRegion("강원특별자치도", "강릉시", 32, 1, "proof"),
                new AttractionEmbeddingTargetRegion("강원특별자치도", "고성군", 32, 2, "proof")
        );

        assertThatThrownBy(() -> validator.validate(invalid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("canonical proof");
    }

    private static Properties yamlProperties() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("embedding-target-regions.yml"));
        Properties properties = factory.getObject();
        assertThat(properties).isNotNull();
        return properties;
    }
}
