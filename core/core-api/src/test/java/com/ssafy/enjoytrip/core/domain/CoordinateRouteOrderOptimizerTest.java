package com.ssafy.enjoytrip.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CoordinateRouteOrderOptimizerTest {
    private final CoordinateRouteOrderOptimizer optimizer = new CoordinateRouteOrderOptimizer();

    @DisplayName("좌표 최적화기는 같은 일자 안에서 가까운 방문 순서를 결정한다")
    @Test
    void optimizesItemsWithinDay() {
        List<RouteCandidate> optimized = optimizer.optimizeByDay(
                List.of(
                        new RouteCandidate("A", 1, 37.0, 127.0),
                        new RouteCandidate("C", 1, 37.0, 129.0),
                        new RouteCandidate("B", 1, 37.0, 128.0)
                ),
                RouteCandidate::day,
                RouteCandidate::latitude,
                RouteCandidate::longitude
        );

        assertThat(optimized).extracting(RouteCandidate::name).containsExactly("A", "B", "C");
    }

    @DisplayName("좌표 최적화기는 일자가 다르면 각 일자 순서만 독립적으로 조정한다")
    @Test
    void optimizesEachDayIndependently() {
        List<RouteCandidate> optimized = optimizer.optimizeByDay(
                List.of(
                        new RouteCandidate("D1-A", 1, 37.0, 127.0),
                        new RouteCandidate("D1-C", 1, 37.0, 129.0),
                        new RouteCandidate("D1-B", 1, 37.0, 128.0),
                        new RouteCandidate("D2-A", 2, 35.0, 127.0),
                        new RouteCandidate("D2-B", 2, 35.0, 128.0)
                ),
                RouteCandidate::day,
                RouteCandidate::latitude,
                RouteCandidate::longitude
        );

        assertThat(optimized).extracting(RouteCandidate::name)
                .containsExactly("D1-A", "D1-B", "D1-C", "D2-A", "D2-B");
    }

    @DisplayName("좌표가 부족하면 해당 순서를 그대로 유지한다")
    @Test
    void keepsOrderWhenCoordinatesAreMissing() {
        List<RouteCandidate> candidates = List.of(
                new RouteCandidate("A", 1, 37.0, 127.0),
                new RouteCandidate("B", 1, null, 128.0)
        );

        List<RouteCandidate> optimized = optimizer.optimizeByDay(
                candidates,
                RouteCandidate::day,
                RouteCandidate::latitude,
                RouteCandidate::longitude
        );

        assertThat(optimized).containsExactlyElementsOf(candidates);
        assertThat(optimizer.hasOptimizableCoordinates(
                candidates,
                RouteCandidate::latitude,
                RouteCandidate::longitude
        )).isFalse();
    }

    @DisplayName("좌표 최적화기는 빈 목록과 단일 항목을 그대로 반환한다")
    @Test
    void keepsEmptyAndSingleItemRoutes() {
        assertThat(optimizer.optimizeByDay(
                List.of(),
                RouteCandidate::day,
                RouteCandidate::latitude,
                RouteCandidate::longitude
        )).isEmpty();

        List<RouteCandidate> single = List.of(new RouteCandidate("A", 1, 37.0, 127.0));

        assertThat(optimizer.optimizeByDay(
                single,
                RouteCandidate::day,
                RouteCandidate::latitude,
                RouteCandidate::longitude
        )).containsExactlyElementsOf(single);
    }

    @DisplayName("좌표 최적화기는 유한하지 않은 좌표가 있으면 순서를 유지한다")
    @Test
    void keepsOrderWhenCoordinatesAreNonFinite() {
        List<RouteCandidate> candidates = List.of(
                new RouteCandidate("A", 1, 37.0, 127.0),
                new RouteCandidate("B", 1, Double.NaN, 128.0),
                new RouteCandidate("C", 1, 38.0, Double.POSITIVE_INFINITY)
        );

        List<RouteCandidate> optimized = optimizer.optimizeByDay(
                candidates,
                RouteCandidate::day,
                RouteCandidate::latitude,
                RouteCandidate::longitude
        );

        assertThat(optimized).containsExactlyElementsOf(candidates);
        assertThat(optimizer.hasOptimizableCoordinates(
                candidates,
                RouteCandidate::latitude,
                RouteCandidate::longitude
        )).isFalse();
    }

    @DisplayName("좌표 최적화기는 같은 좌표의 중복 대상도 결정적으로 처리한다")
    @Test
    void keepsDuplicateCoordinateTargetsDeterministic() {
        List<RouteCandidate> candidates = List.of(
                new RouteCandidate("A", 1, 37.0, 127.0),
                new RouteCandidate("B", 1, 37.0, 127.0),
                new RouteCandidate("C", 1, 37.0, 128.0)
        );

        List<RouteCandidate> optimized = optimizer.optimizeByDay(
                candidates,
                RouteCandidate::day,
                RouteCandidate::latitude,
                RouteCandidate::longitude
        );

        assertThat(optimized).extracting(RouteCandidate::name).containsExactly("A", "B", "C");
        assertThat(optimizer.hasOptimizableCoordinates(
                candidates,
                RouteCandidate::latitude,
                RouteCandidate::longitude
        )).isTrue();
    }

    private record RouteCandidate(
            String name,
            int day,
            Double latitude,
            Double longitude
    ) {
    }
}
