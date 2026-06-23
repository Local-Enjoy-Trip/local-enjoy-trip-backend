package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseRouteTest {
    @DisplayName("코스 경로는 항목 위치를 1부터 연속 값으로 정규화한다")
    @Test
    void normalizesContinuousPositions() {
        CourseRoute route = CourseRoute.ofStops(List.of(
                attractionStop(20L, 20),
                attractionStop(10L, 10)
        ));

        assertThat(route.stops()).extracting(CourseStop::position).containsExactly(1, 2);
    }

    @DisplayName("경로 항목 추가와 삭제와 재정렬은 새 경로를 반환하고 기존 구간을 비운다")
    @Test
    void routeMutationsReturnNewRouteAndClearSegments() {
        CourseRoute route = CourseRoute.planned(
                List.of(attractionStop(10L, 1), attractionStop(20L, 2)),
                List.of(new CourseRouteSegment(1, 1, 2, "WALK", 10, 20))
        );

        CourseRoute added = route.add(attractionStop(30L, 3));
        CourseRoute removed = route.removePosition(1);
        CourseRoute reordered = route.reorder(List.of(2, 1));

        assertThat(added).isNotSameAs(route);
        assertThat(added.segments()).isEmpty();
        assertThat(removed.segments()).isEmpty();
        assertThat(reordered.segments()).isEmpty();
        assertThat(reordered.stops()).extracting(stop -> stop.target().id()).containsExactly(20L, 10L);
    }

    @DisplayName("동일 대상은 서로 다른 위치에 중복으로 추가할 수 있다")
    @Test
    void allowsDuplicateTargets() {
        CourseRoute route = CourseRoute.ofStops(List.of(
                attractionStop(10L, 1),
                attractionStop(10L, 2)
        ));

        assertThat(route.stops()).hasSize(2);
    }

    @DisplayName("계획된 2개 이상 경로는 N-1개 인접 구간만 허용한다")
    @Test
    void rejectsNonAdjacentSegments() {
        assertThatThrownBy(() -> CourseRoute.planned(
                List.of(attractionStop(10L, 1), attractionStop(20L, 2), attractionStop(30L, 3)),
                List.of(new CourseRouteSegment(1, 1, 3, "WALK", 10, 20))
        )).isInstanceOfSatisfying(CoreException.class,
                exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));
    }

    @DisplayName("구간 거리와 시간이 음수이면 경로 구간을 만들 수 없다")
    @Test
    void rejectsNegativeSegmentMetrics() {
        assertThatThrownBy(() -> new CourseRouteSegment(1, 1, 2, "WALK", -1, 0))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));
        assertThatThrownBy(() -> new CourseRouteSegment(1, 1, 2, "WALK", 0, -1))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));
    }

    private static CourseStop attractionStop(Long attractionId, int position) {
        return new CourseStop(
                null,
                CourseStopTarget.attraction(attractionId),
                position,
                1,
                null,
                null,
                null
        );
    }
}
