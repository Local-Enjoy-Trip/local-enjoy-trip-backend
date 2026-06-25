package com.ssafy.enjoytrip.external.aicourse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiCourseGenerationClient {
    private static final String SYSTEM_PROMPT = """
            당신은 서울 동네 여행 코스 전문가입니다.
            주어진 후보 관광지 목록에서만 장소를 선택하세요.
            요청한 장소 수를 반드시 지키세요.
            반드시 아래 JSON 형식으로만 응답하세요:
            {"title": "...", "attractionIds": [...], "reason": "..."}

            title 규칙:
            - 코스를 대표하는 짧고 자연스러운 한국어 제목만 작성하세요.
            - 숫자(장소 수), 속도, 지역 코드, 괄호 안 부가 설명은 절대 포함하지 마세요.
            - 예시: "연인과 성수동 감성 카페 투어", "혼자 떠나는 북촌 골목 산책"
            """;

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ObjectMapper objectMapper;

    public AiCourseGenerationResult generate(AiCourseGenerationInput input) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new AiCourseGenerationException(
                    AiCourseGenerationException.Reason.PROVIDER_ERROR,
                    "Spring AI ChatClient.Builder is unavailable."
            );
        }

        String content = builder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(buildUserPrompt(input))
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new AiCourseGenerationException(
                    AiCourseGenerationException.Reason.BLANK_RESPONSE,
                    "AI course generation provider returned blank content."
            );
        }

        return parse(content);
    }

    private AiCourseGenerationResult parse(String content) {
        try {
            JsonNode root = objectMapper.readerFor(JsonNode.class)
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readValue(content.trim());

            if (root == null || !root.isObject()) {
                throw malformed("Response must be a JSON object.");
            }

            String title = textField(root, "title");
            String reason = textField(root, "reason");

            JsonNode idsNode = root.get("attractionIds");
            if (idsNode == null || !idsNode.isArray()) {
                throw malformed("attractionIds must be an array.");
            }

            List<Long> attractionIds = new ArrayList<>();
            for (JsonNode idNode : idsNode) {
                if (!idNode.isIntegralNumber() || !idNode.canConvertToLong()) {
                    throw malformed("attractionIds must contain only integral numeric ids.");
                }
                attractionIds.add(idNode.longValue());
            }

            return new AiCourseGenerationResult(title, attractionIds, reason);
        } catch (JsonProcessingException exception) {
            throw new AiCourseGenerationException(
                    AiCourseGenerationException.Reason.MALFORMED_RESPONSE,
                    "AI course generation provider returned malformed JSON.",
                    exception
            );
        }
    }

    private static String textField(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node == null || !node.isTextual()) {
            throw malformed("'" + fieldName + "' must be a text field.");
        }
        return node.asText();
    }

    private static AiCourseGenerationException malformed(String message) {
        return new AiCourseGenerationException(
                AiCourseGenerationException.Reason.MALFORMED_RESPONSE,
                message
        );
    }

    private static String buildUserPrompt(AiCourseGenerationInput input) {
        StringBuilder sb = new StringBuilder();

        sb.append("[요청 조건]\n");
        sb.append("- 지역: ").append(nullSafe(input.neighborhood())).append('\n');
        sb.append("- 동행: ").append(nullSafe(input.companionLabel())).append('\n');
        sb.append("- 원하는 하루: ").append(String.join(", ", input.themeLabels())).append('\n');
        sb.append("- 속도: ").append(nullSafe(input.paceLabel()))
                .append(" → ").append(input.placeCount()).append("곳\n\n");

        sb.append("[후보 관광지 목록]\n");
        for (AiCourseGenerationInput.AttractionItem item : input.attractionCandidates()) {
            String address = item.addr2() != null && !item.addr2().isBlank() ? item.addr2() : item.addr1();
            sb.append("ID: ").append(item.id())
                    .append(" | ").append(nullSafe(item.title()))
                    .append(" | ").append(nullSafe(address))
                    .append(" | ").append(nullSafe(item.contentTypeId()))
                    .append(" | ").append(summarize(item.overview()))
                    .append('\n');
        }

        if (!input.referenceCourses().isEmpty()) {
            sb.append("\n[참고 코스]\n");
            int idx = 1;
            for (AiCourseGenerationInput.ReferenceCourse course : input.referenceCourses()) {
                sb.append(idx++).append(". ").append(nullSafe(course.title())).append(": ");
                sb.append(String.join(" → ", course.stopTitles()));
                sb.append('\n');
            }
        }

        if (input.userProfileDescription() != null && !input.userProfileDescription().isBlank()) {
            sb.append("\n[유저 취향 설명]\n");
            sb.append(input.userProfileDescription()).append('\n');
        }

        if (!input.attractionCandidates().isEmpty()) {
            sb.append("\n[특별 지시사항]\n");
            sb.append("- 생성할 코스의 첫 번째 장소는 반드시 후보 목록의 첫 번째 장소인 '")
                    .append(input.attractionCandidates().get(0).title())
                    .append("' (ID: ").append(input.attractionCandidates().get(0).id()).append(")여야 합니다.\n");
        }

        return sb.toString();
    }

    private static String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.length() > 100 ? text.substring(0, 100) + "..." : text;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
