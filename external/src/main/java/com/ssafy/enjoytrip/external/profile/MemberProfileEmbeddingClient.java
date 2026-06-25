package com.ssafy.enjoytrip.external.profile;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberProfileEmbeddingClient {
    private static final String PROVIDER = "openai";
    private static final int EXPECTED_DIMENSION = 1536;
    private static final String PROMPT_TEMPLATE = """
            다음은 사용자가 저장한 장소와 쪽지 목록입니다.
            이 정보를 바탕으로 사용자의 여행 취향을 2~3문장으로 설명해주세요.

            [저장한 장소]
            {attractions}

            [저장한 쪽지]
            {notes}
            """;

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;

    public MemberProfileDescriptionResult embed(MemberProfileInput input) {
        String description = generateDescription(input);
        return embedDescription(description);
    }

    private String generateDescription(MemberProfileInput input) {
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            throw new MemberProfileEmbeddingException(
                    "CHAT_CLIENT_UNAVAILABLE",
                    "ChatClient is unavailable."
            );
        }

        String attractionsText = buildAttractionText(input.attractions());
        String notesText = buildNoteText(input.notes());
        String prompt = PROMPT_TEMPLATE
                .replace("{attractions}", attractionsText)
                .replace("{notes}", notesText);

        return chatClient.prompt(prompt).call().content();
    }

    private MemberProfileDescriptionResult embedDescription(String description) {
        EmbeddingModel model = embeddingModelProvider.getIfAvailable();
        if (model == null) {
            throw new MemberProfileEmbeddingException(
                    "EMBEDDING_PROVIDER_UNAVAILABLE",
                    "EmbeddingModel is unavailable."
            );
        }

        EmbeddingResponse response = model.call(new EmbeddingRequest(List.of(description), null));
        float[] vector = response.getResults().get(0).getOutput();
        String modelName = response.getMetadata().getModel();

        List<Double> embedding = toDoubleList(vector);
        if (embedding.size() != EXPECTED_DIMENSION) {
            throw new MemberProfileEmbeddingException(
                    "EMBEDDING_DIMENSION_MISMATCH",
                    "임베딩 차원 " + embedding.size() + "이 기대값 " + EXPECTED_DIMENSION + "과 다릅니다."
            );
        }

        return new MemberProfileDescriptionResult(
                description,
                PROVIDER,
                modelName == null ? "unknown" : modelName,
                embedding.size(),
                embedding
        );
    }

    private static String buildAttractionText(List<MemberProfileInput.SavedAttractionItem> items) {
        if (items.isEmpty()) {
            return "없음";
        }

        StringBuilder sb = new StringBuilder();
        for (MemberProfileInput.SavedAttractionItem item : items) {
            sb.append("- ").append(item.title());
            if (item.addr1() != null && !item.addr1().isBlank()) {
                sb.append(", ").append(item.addr1());
            }
            if (item.contentTypeId() != null && !item.contentTypeId().isBlank()) {
                sb.append(", 타입: ").append(item.contentTypeId());
            }
            sb.append('\n');
        }
        return sb.toString().strip();
    }

    private static String buildNoteText(List<MemberProfileInput.SavedNoteItem> items) {
        if (items.isEmpty()) {
            return "없음";
        }

        StringBuilder sb = new StringBuilder();
        for (MemberProfileInput.SavedNoteItem item : items) {
            sb.append("- ").append(item.title());
            if (item.category() != null && !item.category().isBlank()) {
                sb.append(", ").append(item.category());
            }
            if (item.tagNames() != null && !item.tagNames().isBlank()) {
                sb.append(", 태그: ").append(item.tagNames());
            }
            sb.append('\n');
        }
        return sb.toString().strip();
    }

    private static List<Double> toDoubleList(float[] vector) {
        List<Double> list = new ArrayList<>(vector.length);
        for (float v : vector) {
            list.add((double) v);
        }
        return list;
    }
}
