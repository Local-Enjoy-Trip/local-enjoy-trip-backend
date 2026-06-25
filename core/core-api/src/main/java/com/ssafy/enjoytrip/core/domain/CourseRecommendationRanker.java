package com.ssafy.enjoytrip.core.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CourseRecommendationRanker {
    private static final double PENALTY_RECENT_7D = 0.30;
    private static final double PENALTY_RECENT_30D = 0.15;
    private static final double PENALTY_CATEGORY_EXCESS = 0.20;
    private static final double PENALTY_TAG_BIAS = 0.10;
    private static final int CATEGORY_QUOTA = 2;

    public List<Course> rerank(
            List<CourseRecommendationCandidate> candidates,
            RerankingContext context,
            int limit
    ) {
        List<ScoredCandidate> scored = candidates.stream()
                .map(c -> new ScoredCandidate(c, computeBaseScore(c, context)))
                .sorted(Comparator.comparingDouble(ScoredCandidate::score))
                .toList();

        Map<String, Integer> categoryCount = new HashMap<>();
        List<Course> result = new ArrayList<>();

        for (ScoredCandidate sc : scored) {
            if (result.size() >= limit) {
                break;
            }

            String category = sc.candidate().dominantCategory();
            int count = categoryCount.getOrDefault(category, 0);
            double categoryPenalty = count >= CATEGORY_QUOTA
                    ? PENALTY_CATEGORY_EXCESS * (count - CATEGORY_QUOTA + 1)
                    : 0.0;

            if (sc.score() + categoryPenalty < Double.MAX_VALUE) {
                result.add(sc.candidate().course());
                if (category != null) {
                    categoryCount.merge(category, 1, Integer::sum);
                }
            }
        }

        return result;
    }

    private double computeBaseScore(
            CourseRecommendationCandidate candidate,
            RerankingContext context
    ) {
        double score = candidate.similarityDistance();
        String courseId = candidate.course().id();

        if (context.viewedWithin7Days().contains(courseId)) {
            score += PENALTY_RECENT_7D;
        } else if (context.viewedWithin30Days().contains(courseId)) {
            score += PENALTY_RECENT_30D;
        }

        if (hasTagBias(candidate.course(), context.memberTagFrequency())) {
            score += PENALTY_TAG_BIAS;
        }

        return score;
    }

    private boolean hasTagBias(Course course, Map<Long, Long> tagFrequency) {
        if (tagFrequency.isEmpty() || course.tags().isEmpty()) {
            return false;
        }

        List<Long> topTagIds = tagFrequency.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        Set<Long> courseTagIds = course.tags().stream()
                .map(CourseTag::tagId)
                .collect(Collectors.toSet());

        return !courseTagIds.isEmpty() && topTagIds.containsAll(courseTagIds);
    }

    private record ScoredCandidate(CourseRecommendationCandidate candidate, double score) {}
}
