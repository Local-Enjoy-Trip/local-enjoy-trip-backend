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
            throw new IllegalStateException(
                    "GMS API 키가 없습니다. 동네 브리핑용 GMS_KEY를 설정하세요."
            );
        }
        if (maxLength <= 0) {
            throw new IllegalStateException("동네 브리핑 maxLength는 양수여야 합니다.");
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
