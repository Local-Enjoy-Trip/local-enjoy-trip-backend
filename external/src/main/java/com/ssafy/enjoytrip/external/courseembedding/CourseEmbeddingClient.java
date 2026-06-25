package com.ssafy.enjoytrip.external.courseembedding;

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
public class CourseEmbeddingClient {
    private static final String PROVIDER = "openai";
    private static final int EXPECTED_DIMENSION = 1536;
    private static final String PROMPT_TEMPLATE = """
            다음 여행 코스를 2~3문장으로 설명하고, 이 코스의 주요 특징을 표현해주세요.

            코스명: {title}
            지역: {regionName}
            태그: {tagNames}
            정류장: {stopTitles}
            """;

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;

    public CourseEmbeddingResult embed(CourseEmbeddingInput input) {
        String description = generateDescription(input);
        return embedDescription(description);
    }

    private String generateDescription(CourseEmbeddingInput input) {
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            throw new CourseEmbeddingException(
                    "CHAT_CLIENT_UNAVAILABLE",
                    "ChatClient is unavailable."
            );
        }

        String prompt = PROMPT_TEMPLATE
                .replace("{title}", nullSafe(input.title()))
                .replace("{regionName}", nullSafe(input.regionName()))
                .replace("{tagNames}", nullSafe(input.tagNames()))
                .replace("{stopTitles}", nullSafe(input.stopTitles()));

        return chatClient.prompt(prompt).call().content();
    }

    private CourseEmbeddingResult embedDescription(String description) {
        EmbeddingModel model = embeddingModelProvider.getIfAvailable();
        if (model == null) {
            throw new CourseEmbeddingException(
                    "EMBEDDING_PROVIDER_UNAVAILABLE",
                    "EmbeddingModel is unavailable."
            );
        }

        EmbeddingResponse response = model.call(new EmbeddingRequest(List.of(description), null));
        float[] vector = response.getResults().get(0).getOutput();
        String modelName = response.getMetadata().getModel();

        List<Double> embedding = toDoubleList(vector);
        if (embedding.size() != EXPECTED_DIMENSION) {
            throw new CourseEmbeddingException(
                    "EMBEDDING_DIMENSION_MISMATCH",
                    "임베딩 차원 " + embedding.size() + "이 기대값 " + EXPECTED_DIMENSION + "과 다릅니다."
            );
        }

        return new CourseEmbeddingResult(
                description,
                PROVIDER,
                modelName == null ? "unknown" : modelName,
                embedding.size(),
                embedding
        );
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private static List<Double> toDoubleList(float[] vector) {
        List<Double> list = new ArrayList<>(vector.length);
        for (float v : vector) {
            list.add((double) v);
        }
        return list;
    }
}
