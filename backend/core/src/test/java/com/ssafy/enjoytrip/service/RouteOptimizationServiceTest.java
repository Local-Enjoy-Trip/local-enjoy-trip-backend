package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Point;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteOptimizationServiceTest {
    private final RouteOptimizationService service = new RouteOptimizationService();

    @DisplayName("경로 최적화는 단순 입력에 빈 순서나 단일 지점 순서를 반환한다")
    @Test
    void optimizeOrderReturnsEmptyOrSinglePointOrderForTrivialInputs() {
        assertArrayEquals(new int[0], service.optimizeOrder(null));
        assertArrayEquals(new int[0], service.optimizeOrder(List.of()));
        assertArrayEquals(new int[]{0}, service.optimizeOrder(List.of(new Point(37.0, 127.0, 0))));
    }

    @DisplayName("경로 최적화는 첫 지점에서 시작해 가까운 지점을 방문한다")
    @Test
    void optimizeOrderStartsFromFirstPointAndVisitsNearestPoints() {
        List<Point> points = List.of(
                new Point(37.000, 127.000, 0),
                new Point(37.010, 127.000, 1),
                new Point(37.020, 127.000, 2)
        );

        int[] order = service.optimizeOrder(points);

        assertArrayEquals(new int[]{0, 1, 2}, order);
    }

    @DisplayName("가장 큰 간격 기준 분할은 큰 구간을 요청 일수로 나눈다")
    @Test
    void splitByLargestGapCutsLargestEdgesIntoRequestedDays() {
        List<Point> orderedPoints = List.of(
                new Point(37.000, 127.000, 0),
                new Point(37.001, 127.000, 1),
                new Point(37.100, 127.000, 2),
                new Point(37.101, 127.000, 3),
                new Point(37.200, 127.000, 4)
        );

        RouteOptimizationService.SplitResult result = service.splitByLargestGap(orderedPoints, 3);

        assertEquals(List.of(List.of(0, 1), List.of(2, 3), List.of(4)), result.days());
        assertEquals(3, result.dayDistances().size());
    }

}
