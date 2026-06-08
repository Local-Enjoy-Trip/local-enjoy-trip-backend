package com.ssafy.enjoytrip.external.embedding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;

class GmsAttractionKeywordExpansionGatewayTest {
    @Test
    void parsesChatCompletionKeywordJsonForEmbeddingInput() throws Exception {
        GmsKeywordExpansionProperties properties = new GmsKeywordExpansionProperties();
        properties.setMaxKeywords(2);
        GmsAttractionKeywordExpansionGateway gateway = new GmsAttractionKeywordExpansionGateway(
                HttpClient.newHttpClient(), new ObjectMapper(), properties);

        var expansion = gateway.parse("""
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"keywords\\":[\\"해질녘 전주천 산책\\",\\"시장 먹거리\\",\\"한옥마을 외곽 골목\\"]}"
                      }
                    }
                  ]
                }
                """);

        assertThat(expansion.keywords()).containsExactly("해질녘 전주천 산책", "시장 먹거리");
        assertThat(expansion.embeddingText()).isEqualTo("해질녘 전주천 산책\n시장 먹거리");
    }
}
