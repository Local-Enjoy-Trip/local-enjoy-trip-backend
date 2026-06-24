package com.ssafy.enjoytrip.external.embedding;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoteEmbeddingClient {
    private static final String PROVIDER = "openai";
    private static final int EXPECTED_DIMENSION = 1536;

    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;

    public NoteEmbeddingResult embed(String content) {
        EmbeddingModel model = embeddingModelProvider.getIfAvailable();
        if (model == null) {
            throw new NoteEmbeddingException("NOTE_EMBEDDING_PROVIDER_UNAVAILABLE",
                    "EmbeddingModel is unavailable.");
        }

        EmbeddingResponse response = model.call(new EmbeddingRequest(List.of(content), null));

        float[] vector = response.getResults().get(0).getOutput();
        String modelName = response.getMetadata().getModel();

        List<Double> embedding = toDoubleList(vector);
        if (embedding.size() != EXPECTED_DIMENSION) {
            throw new NoteEmbeddingException("NOTE_EMBEDDING_DIMENSION_MISMATCH",
                    "임베딩 차원 " + embedding.size() + "이 기대값 " + EXPECTED_DIMENSION + "과 다릅니다.");
        }

        return new NoteEmbeddingResult(PROVIDER, modelName == null ? "unknown" : modelName, embedding.size(), embedding);
    }

    private static List<Double> toDoubleList(float[] vector) {
        List<Double> list = new ArrayList<>(vector.length);
        for (float v : vector) {
            list.add((double) v);
        }
        return list;
    }
}
