package com.ssafy.enjoytrip.external.embedding;

import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingGatewayException;
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
    private String model = "text-embedding-3-large";
    private int dimension = 3072;

    public void assertLiveReady() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AttractionEmbeddingGatewayException("GMS_KEY_MISSING",
                    "GMS API key is missing. Set GMS_KEY so enjoytrip.ai.embedding.gms.api-key is populated.");
        }
        if (url == null || url.isBlank()) {
            throw new AttractionEmbeddingGatewayException("GMS_URL_MISSING", "GMS embedding URL is missing.");
        }
        if (dimension != 3072) {
            throw new AttractionEmbeddingGatewayException("GMS_DIMENSION_UNSUPPORTED",
                    "text-embedding-3-large backfill expects 3072 dimensions.");
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
