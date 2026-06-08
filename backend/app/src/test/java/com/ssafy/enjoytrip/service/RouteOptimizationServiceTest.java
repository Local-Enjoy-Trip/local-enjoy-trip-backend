package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Point;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("service")
class RouteOptimizationServiceTest {
    private final RouteOptimizationService service = new RouteOptimizationService();

    @Nested
    class Parsing {
        @Test
        void parsesPipeDelimitedLatitudeLongitudePairsWithOriginalIndexes() {
            List<Point> points = service.parsePoints("37.5665,126.9780 | 35.1796,129.0756");

            assertThat(points).containsExactly(
                    new Point(37.5665, 126.9780, 0),
                    new Point(35.1796, 129.0756, 1)
            );
        }

        @Test
        void returnsEmptyListForMissingInputAndRejectsMalformedCoordinates() {
            assertThat(service.parsePoints(null)).isEmpty();
            assertThat(service.parsePoints(" ")).isEmpty();

            assertThatThrownBy(() -> service.parsePoints("37.5|bad,126.9"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    class Optimization {
        @Test
        void handlesEmptyAndSinglePointRoutes() {
            assertThat(service.optimizeOrder(null)).isEmpty();
            assertThat(service.optimizeOrder(List.of())).isEmpty();
            assertThat(service.optimizeOrder(List.of(new Point(37.5, 127.0, 0)))).containsExactly(0);
        }

        @Test
        void returnsPermutationStartingAtFirstPointAndDistanceEstimate() {
            List<Point> points = List.of(
                    new Point(37.5665, 126.9780, 0),
                    new Point(37.5700, 126.9820, 1),
                    new Point(35.1796, 129.0756, 2),
                    new Point(33.4996, 126.5312, 3)
            );

            int[] order = service.optimizeOrder(points);

            assertThat(order).hasSize(points.size());
            assertThat(order[0]).isZero();
            assertThat(Arrays.stream(order).boxed()).containsExactlyInAnyOrder(0, 1, 2, 3);
            assertThat(service.estimateTotalDistanceKm(points, order)).isPositive();
            assertThat(service.formatDouble(1.23456)).isEqualTo("1.2346");
        }
    }

    @Nested
    class Splitting {
        @Test
        void splitByLargestGapClampsDayCountAndReportsIntraDayDistances() {
            List<Point> orderedPoints = List.of(
                    new Point(37.5665, 126.9780, 0),
                    new Point(37.5700, 126.9820, 1),
                    new Point(35.1796, 129.0756, 2)
            );

            RouteOptimizationService.SplitResult result = service.splitByLargestGap(orderedPoints, 10);

            assertThat(result.days()).hasSize(3);
            assertThat(result.days()).containsExactly(List.of(0), List.of(1), List.of(2));
            assertThat(result.dayDistances()).containsExactly(0.0, 0.0, 0.0);
        }

        @Test
        void splitHandlesEmptyAndSinglePointInputs() {
            assertThat(service.splitByLargestGap(null, 2).days()).isEmpty();

            RouteOptimizationService.SplitResult one = service.splitByLargestGap(List.of(new Point(37.5, 127.0, 0)), 3);

            assertThat(one.days()).containsExactly(List.of(0));
            assertThat(one.dayDistances()).containsExactly(0.0);
        }
    }
}
