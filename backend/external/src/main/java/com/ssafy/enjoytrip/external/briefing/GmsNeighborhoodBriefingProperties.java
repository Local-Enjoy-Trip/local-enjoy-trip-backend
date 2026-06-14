package com.ssafy.enjoytrip.external.briefing;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "enjoytrip.ai.briefing.gms")
public class GmsNeighborhoodBriefingProperties {
    private String apiKey = "";
    private int maxLength = 160;

    public void assertLiveReady() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GMS API key is missing. Set GMS_KEY for neighborhood briefing.");
        }
        if (maxLength <= 0) {
            throw new IllegalStateException("Neighborhood briefing maxLength must be positive.");
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}
