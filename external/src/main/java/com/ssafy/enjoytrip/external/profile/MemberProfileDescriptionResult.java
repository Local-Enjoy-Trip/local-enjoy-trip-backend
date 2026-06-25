package com.ssafy.enjoytrip.external.profile;

import java.util.List;

public record MemberProfileDescriptionResult(
        String description,
        String provider,
        String model,
        int dimension,
        List<Double> embedding
) {
    public MemberProfileDescriptionResult {
        embedding = List.copyOf(embedding);
    }
}
