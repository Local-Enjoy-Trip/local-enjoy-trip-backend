package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseRoutePlannerTest {
    private final CourseRoutePlanner planner = new DefaultCourseRoutePlanner();

    @DisplayName("경로 플래너는 입력 순서대로 N-1 인접 구간을 생성한다")
    @Test
    void plansAdjacentSegmentsInInputOrder() {
        CourseRoute route = planner.plan(List.of(
                point(attractionStop(10L, 2), 37.1, 127.1),
                point(attractionStop(20L, 1), 37.0, 127.0),
                point(attractionStop(30L, 3), 37.2, 127.2)
        ));

        assertThat(route.stops()).extracting(CourseStop::position).containsExactly(1, 2, 3);
        assertThat(route.stops()).extracting(stop -> stop.target().id()).containsExactly(10L, 20L, 30L);
        assertThat(route.segments()).hasSize(2);
        assertThat(route.segments())
                .extracting(segment -> segment.fromPosition() + ":" + segment.toPosition())
                .containsExactly("1:2", "2:3");
        assertThat(route.summary().totalDistanceMeters()).isPositive();
    }

    @DisplayName("경로 플래너는 0개 또는 1개 항목에는 구간을 생성하지 않는다")
    @Test
    void keepsZeroSegmentsForZeroOrSingleStop() {
        CourseRoute empty = planner.plan(List.of());
        CourseRoute single = planner.plan(List.of(point(attractionStop(10L, 1), 37.0, 127.0)));

        assertThat(empty.segments()).isEmpty();
        assertThat(single.segments()).isEmpty();
    }

    @DisplayName("경로 플래너는 1개 항목도 좌표가 없으면 실패로 처리한다")
    @Test
    void rejectsMissingCoordinatesForSingleStop() {
        assertThatThrownBy(() -> planner.plan(List.of(
                point(attractionStop(10L, 1), null, null)
        ))).isInstanceOfSatisfying(CoreException.class,
                exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));
    }

    @DisplayName("경로 플래너는 2개 이상 항목의 좌표 누락을 실패로 처리한다")
    @Test
    void rejectsMissingCoordinatesForMultipleStops() {
        assertThatThrownBy(() -> planner.plan(List.of(
                point(attractionStop(10L, 1), 37.0, 127.0),
                point(attractionStop(20L, 2), null, 127.1)
        ))).isInstanceOfSatisfying(CoreException.class,
                exception -> assertThat(exception.errorType()).isEqualTo(COURSE_INVALID_ITEM));
    }

    private static CourseStopPoint point(CourseStop stop, Double latitude, Double longitude) {
        return new CourseStopPoint(stop, stop.title(), latitude, longitude);
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
