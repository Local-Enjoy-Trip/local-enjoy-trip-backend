package com.ssafy.enjoytrip.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseTest {

    @DisplayName("구간 수는 정류장 수 - 1이다")
    @Test
    void segmentCountIsStopCountMinusOne() {
        assertThat(courseWith(0).segmentCount()).isEqualTo(0);
        assertThat(courseWith(1).segmentCount()).isEqualTo(0);
        assertThat(courseWith(2).segmentCount()).isEqualTo(1);
        assertThat(courseWith(3).segmentCount()).isEqualTo(2);
    }

    @DisplayName("총 거리는 각 정류장의 distanceToNext 합산이다")
    @Test
    void totalDistanceMetersIsSumOfNextDistances() {
        Course course = courseWithStops(
                stopWithMetrics(1, 100, 60),
                stopWithMetrics(2, 200, 120),
                stopNoMetrics(3)
        );

        assertThat(course.totalDistanceMeters()).isEqualTo(300);
    }

    @DisplayName("총 소요 시간은 각 정류장의 durationToNext 합산이다")
    @Test
    void totalDurationSecondsIsSumOfNextDurations() {
        Course course = courseWithStops(
                stopWithMetrics(1, 100, 60),
                stopWithMetrics(2, 200, 120),
                stopNoMetrics(3)
        );

        assertThat(course.totalDurationSeconds()).isEqualTo(180);
    }

    @DisplayName("null인 distanceToNext/durationToNext는 0으로 취급한다")
    @Test
    void nullMetricsTreatedAsZero() {
        Course course = courseWithStops(stopNoMetrics(1));

        assertThat(course.totalDistanceMeters()).isZero();
        assertThat(course.totalDurationSeconds()).isZero();
    }

    @DisplayName("withStops는 stops를 교체한 새 Course를 반환한다")
    @Test
    void withStopsReturnsNewCourseWithUpdatedStops() {
        Course original = courseWith(0);
        CourseStop newStop = stopNoMetrics(1);

        Course updated = original.withStops(List.of(newStop));

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.stops()).containsExactly(newStop);
        assertThat(original.stops()).isEmpty();
    }

    @DisplayName("withStartLocation은 시작 좌표를 교체한 새 Course를 반환한다")
    @Test
    void withStartLocationUpdatesCoordinates() {
        Course original = courseWith(0);
        CourseStopPoint point = new CourseStopPoint(stopNoMetrics(1), "장소", 37.5, 127.0);

        Course updated = original.withStartLocation(point);

        assertThat(updated.startLocation()).isNotNull();
        assertThat(updated.startLocation().latitude()).isEqualTo(37.5);
        assertThat(updated.startLocation().longitude()).isEqualTo(127.0);
        assertThat(original.startLocation()).isNull();
    }

    private static Course courseWith(int stopCount) {
        List<CourseStop> stops = IntStream.rangeClosed(1, stopCount)
                .mapToObj(i -> stopNoMetrics(i))
                .toList();
        return new Course("c1", 1L, "테스트 코스", "서울", null, null, null, 0, null, "", "", stops, List.of());
    }

    private static Course courseWithStops(CourseStop... stops) {
        return new Course("c1", 1L, "테스트 코스", "서울", null, null, null, 0, null, "", "", List.of(stops), List.of());
    }

    private static CourseStop stopWithMetrics(int position, int distanceToNext, int durationToNext) {
        return new CourseStop(null, CourseStopTarget.attraction((long) position), position,
                null, distanceToNext, durationToNext);
    }

    private static CourseStop stopNoMetrics(int position) {
        return new CourseStop(null, CourseStopTarget.attraction((long) position), position,
                null, null, null);
    }
}
