package com.ssafy.enjoytrip.external.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingGatewayException;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingResult;
import com.ssafy.enjoytrip.repository.embedding.AttractionEmbeddingGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GmsAttractionEmbeddingGateway implements AttractionEmbeddingGateway {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final GmsEmbeddingProperties properties;

    @Override
    public AttractionEmbeddingResult embed(String sourceText) {
        properties.assertLiveReady();
        try {
            HttpResponse<String> response = httpClient.send(request(sourceText), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AttractionEmbeddingGatewayException("GMS_HTTP_" + response.statusCode(),
                        "GMS embedding request failed with HTTP " + response.statusCode());
            }
            return parse(response.body());
        } catch (IOException ex) {
            throw new AttractionEmbeddingGatewayException("GMS_IO_ERROR", "GMS embedding request failed", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AttractionEmbeddingGatewayException("GMS_INTERRUPTED", "GMS embedding request was interrupted", ex);
        }
    }

    private HttpRequest request(String sourceText) throws IOException {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", properties.getModel());
        payload.put("input", sourceText);
        return HttpRequest.newBuilder(URI.create(properties.getUrl()))
                .timeout(properties.getTimeout())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                .build();
    }

    private AttractionEmbeddingResult parse(String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode embeddingNode = root.path("data").path(0).path("embedding");
        if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
            throw new AttractionEmbeddingGatewayException("GMS_INVALID_RESPONSE", "GMS response did not contain data[0].embedding.");
        }
        List<Double> embedding = new ArrayList<>(embeddingNode.size());
        for (JsonNode item : embeddingNode) {
            embedding.add(item.asDouble());
        }
        if (embedding.size() != properties.getDimension()) {
            throw new AttractionEmbeddingGatewayException("GMS_DIMENSION_MISMATCH",
                    "GMS embedding dimension " + embedding.size() + " did not match expected " + properties.getDimension());
        }
        String model = root.path("model").asText(properties.getModel());
        return new AttractionEmbeddingResult(properties.getProvider(), model, embedding.size(), embedding);
    }
}
