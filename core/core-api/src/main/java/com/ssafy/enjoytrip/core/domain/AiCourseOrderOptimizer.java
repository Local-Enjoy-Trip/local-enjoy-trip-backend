package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.external.courseorder.CourseOrderRecommendationItem;
import com.ssafy.enjoytrip.external.courseorder.CourseOrderRecommendationRequest;
import com.ssafy.enjoytrip.external.courseorder.CourseOrderRecommendationResult;
import com.ssafy.enjoytrip.external.courseorder.SpringAiCourseOrderRecommendationClient;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AiCourseOrderOptimizer implements CourseOrderOptimizer {
    private static final String COORDINATE_INSUFFICIENT = "COORDINATE_INSUFFICIENT";
    private static final String RECOMMENDATION_FAILED = "RECOMMENDATION_FAILED";

    private final CourseOrderPreviewReader courseOrderPreviewReader;
    private final CourseRoutePlanner courseRoutePlanner;
    private final CoordinateRouteOrderOptimizer coordinateRouteOrderOptimizer;
    private final SpringAiCourseOrderRecommendationClient courseOrderRecommendationClient;

    @Override
    public Course recommend(Course course, CourseOrderOptimizationContext context) {
        CourseOrderPreview preview = courseOrderPreviewReader.read(course);
        if (preview.itemCount() < 2) {
            return course;
        }
        if (!preview.hasCompleteCoordinates()) {
            return fallbackCurrent(preview);
        }

        try {
            return course.withStops(planPreviewRoute(recommendByAi(preview, context)));
        } catch (RuntimeException exception) {
            return fallbackOptimized(preview, exception);
        }
    }

    private Course fallbackCurrent(CourseOrderPreview preview) {
        logRecommendationFallback(preview, COORDINATE_INSUFFICIENT, null);
        return preview.course();
    }

    private Course fallbackOptimized(CourseOrderPreview preview, RuntimeException exception) {
        logRecommendationFallback(preview, RECOMMENDATION_FAILED, exception);
        List<CourseOrderPreviewItem> optimizedItems = coordinateRouteOrderOptimizer.optimizeByDay(
                preview.items(),
                item -> 1,
                CourseOrderPreviewItem::latitude,
                CourseOrderPreviewItem::longitude
        );
        return preview.course().withStops(planPreviewRoute(optimizedItems));
    }

    private List<CourseOrderPreviewItem> recommendByAi(CourseOrderPreview preview,
                                                       CourseOrderOptimizationContext context) {
        CourseOrderRecommendationResult result = courseOrderRecommendationClient.recommend(
                toExternalRequest(preview, context)
        );
        requireValidRecommendation(preview, result);
        return orderByRecommendedIds(preview, result.orderedItemIds());
    }

    private List<CourseStop> planPreviewRoute(List<CourseOrderPreviewItem> orderedItems) {
        List<CourseStop> orderedStops = orderedItems.stream()
                .map(item -> item.stop().withTitle(item.title()))
                .toList();
        Map<Long, CourseOrderPreviewItem> itemsById = itemsById(orderedItems);
        List<CourseStopPoint> points = orderedStops.stream()
                .map(stop -> pointOf(stop, itemsById))
                .toList();
        List<CourseStop> plannedStops = courseRoutePlanner.plan(points);
        requireCompleteNextMetrics(plannedStops);
        return plannedStops;
    }

    private static CourseStopPoint pointOf(CourseStop stop, Map<Long, CourseOrderPreviewItem> itemsById) {
        CourseOrderPreviewItem item = itemsById.get(stop.id());
        return new CourseStopPoint(stop, item.title(), item.latitude(), item.longitude());
    }

    private static CourseOrderRecommendationRequest toExternalRequest(CourseOrderPreview preview,
                                                                      CourseOrderOptimizationContext context) {
        return new CourseOrderRecommendationRequest(
                preview.course().id(),
                context.currentLatitude(),
                context.currentLongitude(),
                preview.items().stream()
                        .map(AiCourseOrderOptimizer::toExternalItem)
                        .toList()
        );
    }

    private static CourseOrderRecommendationItem toExternalItem(CourseOrderPreviewItem item) {
        CourseStop stop = item.stop();
        return new CourseOrderRecommendationItem(
                item.id(),
                stop.target().type().name(),
                stop.target().id(),
                item.title(),
                stop.position(),
                item.contentTypeId(),
                item.latitude(),
                item.longitude()
        );
    }

    private static void requireValidRecommendation(CourseOrderPreview preview,
                                                   CourseOrderRecommendationResult result) {
        List<Long> orderedIds = result.orderedItemIds();
        if (orderedIds.isEmpty()) {
            throw new IllegalStateException("Course order recommendation returned no items.");
        }

        Set<Long> expectedIds = new HashSet<>();
        for (CourseOrderPreviewItem item : preview.items()) {
            expectedIds.add(item.id());
        }

        Set<Long> seenIds = new HashSet<>();
        for (Long orderedId : orderedIds) {
            requireExpectedItemId(expectedIds, seenIds, orderedId);
        }

        if (seenIds.size() != expectedIds.size() || !seenIds.containsAll(expectedIds)) {
            throw new IllegalStateException("Course order recommendation missed course items.");
        }
    }

    private static void requireExpectedItemId(Set<Long> expectedIds, Set<Long> seenIds, Long orderedId) {
        if (!seenIds.add(orderedId)) {
            throw new IllegalStateException("Course order recommendation returned duplicate items.");
        }
        if (!expectedIds.contains(orderedId)) {
            throw new IllegalStateException("Course order recommendation returned unknown items.");
        }
    }

    private static List<CourseOrderPreviewItem> orderByRecommendedIds(CourseOrderPreview preview,
                                                                      List<Long> orderedIds) {
        Map<Long, CourseOrderPreviewItem> itemsById = itemsById(preview.items());
        return orderedIds.stream()
                .map(itemsById::get)
                .toList();
    }

    private static Map<Long, CourseOrderPreviewItem> itemsById(List<CourseOrderPreviewItem> items) {
        Map<Long, CourseOrderPreviewItem> itemsById = new HashMap<>();
        for (CourseOrderPreviewItem item : items) {
            itemsById.put(item.id(), item);
        }
        return itemsById;
    }

    private static void requireCompleteNextMetrics(List<CourseStop> stops) {
        if (stops.size() < 2) {
            return;
        }
        for (int index = 0; index < stops.size() - 1; index++) {
            CourseStop stop = stops.get(index);
            if (stop.distanceToNext() == null || stop.durationToNext() == null) {
                throw new CoreException(COURSE_INVALID_ITEM);
            }
        }
    }

    private static void logRecommendationFallback(CourseOrderPreview preview,
                                                  String reason,
                                                  RuntimeException exception) {
        String providerException = exception == null ? "" : exception.getClass().getName();
        log.warn(
                "Course order recommendation fallback: reason={} courseId={} ownerMemberId={} "
                        + "itemCount={} providerException={}",
                reason,
                preview.course().id(),
                preview.course().ownerMemberId(),
                preview.itemCount(),
                providerException
        );
    }
}
