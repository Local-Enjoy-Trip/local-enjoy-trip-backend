package com.ssafy.enjoytrip.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingGatewayException;
import com.ssafy.enjoytrip.external.ExternalClientTestSupport.FakeHttpClient;
import com.ssafy.enjoytrip.external.embedding.GmsAttractionEmbeddingGateway;
import com.ssafy.enjoytrip.external.embedding.GmsEmbeddingProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GmsAttractionEmbeddingGatewayTest {
    @Test
    void sendsOpenAiCompatibleGmsEmbeddingRequestWithBearerKeyAndParsesVector() {
        GmsEmbeddingProperties properties = properties();
        FakeHttpClient http = new FakeHttpClient()
                .enqueue(200, embeddingResponse());
        GmsAttractionEmbeddingGateway gateway = new GmsAttractionEmbeddingGateway(http, new ObjectMapper(), properties);

        var result = gateway.embed("강릉 여행지");

        assertThat(result.provider()).isEqualTo("gms");
        assertThat(result.model()).isEqualTo("text-embedding-3-large");
        assertThat(result.dimension()).isEqualTo(3072);
        assertThat(result.embedding()).startsWith(0.1, 0.2, 0.3);
        assertThat(http.requests()).hasSize(1);
        assertThat(http.requests().getFirst().uri().toString()).isEqualTo("https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings");
        assertThat(http.requests().getFirst().headers().firstValue("Authorization")).contains("Bearer test-key");
    }

    @Test
    void refusesLiveCallWhenGmsApiKeyIsMissing() {
        GmsEmbeddingProperties properties = properties();
        properties.setApiKey("");
        GmsAttractionEmbeddingGateway gateway = new GmsAttractionEmbeddingGateway(new FakeHttpClient(), new ObjectMapper(), properties);

        assertThatThrownBy(() -> gateway.embed("전주 여행지"))
                .isInstanceOf(AttractionEmbeddingGatewayException.class)
                .hasMessageContaining("GMS API key is missing");
    }

    private static GmsEmbeddingProperties properties() {
        GmsEmbeddingProperties properties = new GmsEmbeddingProperties();
        properties.setApiKey("test-key");
        properties.setTimeout(Duration.ofSeconds(1));
        return properties;
    }

    private static String embeddingResponse() {
        StringBuilder builder = new StringBuilder("{\"data\":[{\"embedding\":[");
        for (int i = 0; i < 3072; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append((i % 10 + 1) / 10.0);
        }
        return builder.append("]}],\"model\":\"text-embedding-3-large\"}").toString();
    }
}
