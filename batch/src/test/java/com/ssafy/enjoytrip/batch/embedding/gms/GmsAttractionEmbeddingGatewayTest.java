package com.ssafy.enjoytrip.batch.embedding.gms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingGatewayException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GmsAttractionEmbeddingGatewayTest {
    @DisplayName("GMS 임베딩은 Bearer 키로 OpenAI 호환 요청을 보내고 벡터를 파싱한다")
    @Test
    void sendsOpenAiCompatibleGmsEmbeddingRequestWithBearerKeyAndParsesVector() {
        GmsEmbeddingProperties properties = properties();
        FakeHttpClient http = new FakeHttpClient()
                .enqueue(200, embeddingResponse());
        GmsAttractionEmbeddingGateway gateway = new GmsAttractionEmbeddingGateway(http, new ObjectMapper(), properties);

        var result = gateway.embed("강릉 여행지");

        assertThat(result.provider()).isEqualTo("gms");
        assertThat(result.model()).isEqualTo("text-embedding-3-small");
        assertThat(result.dimension()).isEqualTo(1536);
        assertThat(result.embedding()).startsWith(0.1, 0.2, 0.3);
        assertThat(http.requests()).hasSize(1);
        assertThat(http.requests().getFirst().uri().toString()).isEqualTo("https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings");
        assertThat(http.requests().getFirst().headers().firstValue("Authorization")).contains("Bearer test-key");
    }

    @DisplayName("GMS API 키가 없으면 실제 호출을 거부한다")
    @Test
    void refusesLiveCallWhenGmsApiKeyIsMissing() {
        GmsEmbeddingProperties properties = properties();
        properties.setApiKey("");
        GmsAttractionEmbeddingGateway gateway = new GmsAttractionEmbeddingGateway(new FakeHttpClient(), new ObjectMapper(), properties);

        assertThatThrownBy(() -> gateway.embed("전주 여행지"))
                .isInstanceOf(AttractionEmbeddingGatewayException.class)
                .hasMessageContaining("GMS API 키가 없습니다");
    }

    private static GmsEmbeddingProperties properties() {
        GmsEmbeddingProperties properties = new GmsEmbeddingProperties();
        properties.setApiKey("test-key");
        properties.setTimeout(Duration.ofSeconds(1));
        return properties;
    }

    private static String embeddingResponse() {
        StringBuilder builder = new StringBuilder("{\"data\":[{\"embedding\":[");
        for (int i = 0; i < 1536; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append((i % 10 + 1) / 10.0);
        }
        return builder.append("]}],\"model\":\"text-embedding-3-small\"}").toString();
    }
}
