package com.ssafy.enjoytrip.batch;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "enjoytrip.ai.embedding.backfill")
public class AttractionEmbeddingBatchProperties {
    private boolean failOnOutsideTargetEmbeddings = true;

    public boolean isFailOnOutsideTargetEmbeddings() { return failOnOutsideTargetEmbeddings; }
    public void setFailOnOutsideTargetEmbeddings(boolean failOnOutsideTargetEmbeddings) {
        this.failOnOutsideTargetEmbeddings = failOnOutsideTargetEmbeddings;
    }
}
