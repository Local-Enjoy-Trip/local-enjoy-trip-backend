package com.ssafy.enjoytrip.external.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ssafy.enjoytrip.domain.embedding.AttractionEmbeddingGatewayException;
import com.ssafy.enjoytrip.domain.embedding.AttractionKeywordExpansion;
import com.ssafy.enjoytrip.repository.embedding.AttractionKeywordExpansionGateway;
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
public class GmsAttractionKeywordExpansionGateway implements AttractionKeywordExpansionGateway {
    private static final String DEVELOPER_PROMPT = """
            너는 로컬레터(Local Letter) 서비스의 관광지 의미확장 편집자다.
            로컬레터 기획 원칙:
            - 장소를 그대로 추천하지 말고 시기 기반 로컬 경험으로 재구성한다.
            - 계절, 날짜, 날씨, 시간대, 동행자, 사용자 취향을 반영할 수 있는 검색/추천 키워드로 확장한다.
            - 전주는 먹거리, 전통문화, 골목, 시장, 야간 산책이 강하다.
            - 강릉은 바다, 커피, 계절 여행, 여름/겨울 브리핑이 강하다.
            - 경험 예시는 '해질녘 전주천 산책', '겨울 시장 먹거리', '비 오는 날 창가 카페', '여름밤 바다 산책'처럼 쓴다.
            출력은 JSON 객체 하나만 허용한다. 설명 문장, markdown, 코드블록을 쓰지 마라.
            JSON schema: {"keywords":["키워드 또는 경험 문구", "..."]}
            """;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final GmsKeywordExpansionProperties properties;

    @Override
    public AttractionKeywordExpansion expand(String attractionSourceText) {
        properties.assertLiveReady();
        try {
            HttpResponse<String> response = httpClient.send(
                    request(attractionSourceText),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AttractionEmbeddingGatewayException("GMS_CHAT_HTTP_" + response.statusCode(),
                        "GMS keyword expansion request failed with HTTP " + response.statusCode());
            }
            return parse(response.body());
        } catch (IOException ex) {
            throw new AttractionEmbeddingGatewayException(
                    "GMS_CHAT_IO_ERROR",
                    "GMS keyword expansion request failed",
                    ex
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AttractionEmbeddingGatewayException(
                    "GMS_CHAT_INTERRUPTED",
                    "GMS keyword expansion request was interrupted",
                    ex
            );
        }
    }

    private HttpRequest request(String attractionSourceText) throws IOException {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", properties.getModel());
        ArrayNode messages = payload.putArray("messages");
        messages.addObject()
                .put("role", "developer")
                .put("content", DEVELOPER_PROMPT);
        messages.addObject()
                .put("role", "user")
                .put("content", userPrompt(attractionSourceText));
        return HttpRequest.newBuilder(URI.create(properties.getUrl()))
                .timeout(properties.getTimeout())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(payload),
                        StandardCharsets.UTF_8
                ))
                .build();
    }

    private String userPrompt(String attractionSourceText) {
        return """
                다음 TourAPI 관광지 원본 데이터를 로컬레터 검색/추천용 의미확장 키워드로 바꿔라.
                요구사항:
                - 원본 장소명만 반복하지 말고 경험 문구를 포함한다.
                - 계절/시간대/날씨/취향/동행자 변형을 포함한다.
                - 임베딩 입력으로 바로 사용할 것이므로 짧고 구체적인 한국어 키워드/문구만 만든다.
                - 최대 %d개.

                원본 데이터:
                %s
                """.formatted(properties.getMaxKeywords(), attractionSourceText);
    }

    AttractionKeywordExpansion parse(String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        String content = root.path("choices").path(0).path("message").path("content").asText("");
        List<String> keywords = parseKeywords(content);
        if (keywords.isEmpty()) {
            throw new AttractionEmbeddingGatewayException("GMS_CHAT_INVALID_RESPONSE",
                    "GMS keyword expansion response did not contain keywords.");
        }
        return new AttractionKeywordExpansion(keywords.stream()
                .limit(properties.getMaxKeywords())
                .toList());
    }

    private List<String> parseKeywords(String content) throws IOException {
        String normalized = stripCodeFence(content);
        if (normalized.isBlank()) {
            return List.of();
        }
        try {
            JsonNode parsed = objectMapper.readTree(normalized);
            JsonNode keywordNode = parsed.isArray() ? parsed : parsed.path("keywords");
            if (keywordNode.isArray()) {
                List<String> keywords = new ArrayList<>(keywordNode.size());
                for (JsonNode item : keywordNode) {
                    String keyword = item.asText("").strip();
                    if (!keyword.isEmpty()) {
                        keywords.add(keyword);
                    }
                }
                return keywords;
            }
        } catch (IOException ignored) {
            // Some models may return plain lines despite the JSON instruction. Fall back to line parsing.
        }
        return normalized.lines()
                .map(line -> line.replaceFirst("^[-*\\d.\\s]+", "").strip())
                .filter(line -> !line.isEmpty())
                .toList();
    }

    private static String stripCodeFence(String content) {
        String normalized = content == null ? "" : content.strip();
        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```[a-zA-Z]*\\s*", "");
            normalized = normalized.replaceFirst("\\s*```$", "");
        }
        return normalized.strip();
    }
}
