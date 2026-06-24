package com.ssafy.enjoytrip.batch.embedding.gms;

import com.ssafy.enjoytrip.batch.embedding.AttractionEmbeddingGatewayException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "enjoytrip.ai.embedding.gms")
public class GmsEmbeddingProperties {
    private String url = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings";
    private String apiKey = "";
    private Duration timeout = Duration.ofSeconds(30);
    private String provider = "gms";
    private String model = "text-embedding-3-small";
    private int dimension = 1536;

    public void assertLiveReady() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AttractionEmbeddingGatewayException("GMS_KEY_MISSING",
                    "GMS API 키가 없습니다. "
                            + "GMS_KEY를 설정해 enjoytrip.ai.embedding.gms.api-key "
                            + "값이 채워지도록 하세요.");
        }
        if (url == null || url.isBlank()) {
            throw new AttractionEmbeddingGatewayException(
                    "GMS_URL_MISSING",
                    "GMS 임베딩 URL이 없습니다."
            );
        }
        if (dimension != 1536) {
            throw new AttractionEmbeddingGatewayException("GMS_DIMENSION_UNSUPPORTED",
                    "text-embedding-3-small 백필은 1536차원을 기대합니다.");
        }
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public Duration getTimeout() { return timeout; }
    public void setTimeout(Duration timeout) { this.timeout = timeout; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getDimension() { return dimension; }
    public void setDimension(int dimension) { this.dimension = dimension; }
}
