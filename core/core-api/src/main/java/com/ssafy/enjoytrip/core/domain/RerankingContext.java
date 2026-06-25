package com.ssafy.enjoytrip.core.domain;

import java.util.Map;
import java.util.Set;

public record RerankingContext(
        Set<String> viewedWithin7Days,
        Set<String> viewedWithin30Days,
        Map<Long, Long> memberTagFrequency
) {
    public static RerankingContext empty() {
        return new RerankingContext(Set.of(), Set.of(), Map.of());
    }
}
