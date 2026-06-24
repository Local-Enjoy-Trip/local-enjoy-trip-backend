package com.ssafy.enjoytrip.core.domain;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.COURSE_INVALID_ITEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseStopTest {

    @DisplayName("target이 null이면 정류장을 만들 수 없다")
    @Test
    void rejectsNullTarget() {
        assertThatThrownBy(() -> new CourseStop(null, null, 1, null, null, null))
                .isInstanceOfSatisfying(CoreException.class,
                        e -> assertThat(e.errorType()).isEqualTo(COURSE_INVALID_ITEM));
    }

    @DisplayName("position이 0 이하이면 정류장을 만들 수 없다")
    @Test
    void rejectsNonPositivePosition() {
        CourseStopTarget target = CourseStopTarget.attraction(1L);
        assertThatThrownBy(() -> new CourseStop(null, target, 0, null, null, null))
                .isInstanceOfSatisfying(CoreException.class,
                        e -> assertThat(e.errorType()).isEqualTo(COURSE_INVALID_ITEM));
    }

    @DisplayName("distanceToNext가 음수이면 정류장을 만들 수 없다")
    @Test
    void rejectsNegativeDistanceToNext() {
        CourseStopTarget target = CourseStopTarget.attraction(1L);
        assertThatThrownBy(() -> new CourseStop(null, target, 1, null, -1, null))
                .isInstanceOfSatisfying(CoreException.class,
                        e -> assertThat(e.errorType()).isEqualTo(COURSE_INVALID_ITEM));
    }

    @DisplayName("durationToNext가 음수이면 정류장을 만들 수 없다")
    @Test
    void rejectsNegativeDurationToNext() {
        CourseStopTarget target = CourseStopTarget.attraction(1L);
        assertThatThrownBy(() -> new CourseStop(null, target, 1, null, null, -1))
                .isInstanceOfSatisfying(CoreException.class,
                        e -> assertThat(e.errorType()).isEqualTo(COURSE_INVALID_ITEM));
    }

    @DisplayName("distanceToNext와 durationToNext는 0이면 허용한다")
    @Test
    void allowsZeroMetrics() {
        CourseStop stop = new CourseStop(null, CourseStopTarget.attraction(1L), 1, null, 0, 0);

        assertThat(stop.distanceToNext()).isZero();
        assertThat(stop.durationToNext()).isZero();
    }

    @DisplayName("withNextMetrics는 distanceToNext와 durationToNext를 교체한 새 정류장을 반환한다")
    @Test
    void withNextMetricsReturnsNewStopWithUpdatedMetrics() {
        CourseStop original = new CourseStop(null, CourseStopTarget.attraction(1L), 1, null, null, null);

        CourseStop updated = original.withNextMetrics(500, 357);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.distanceToNext()).isEqualTo(500);
        assertThat(updated.durationToNext()).isEqualTo(357);
        assertThat(original.distanceToNext()).isNull();
    }

    @DisplayName("withPosition은 position만 교체한 새 정류장을 반환한다")
    @Test
    void withPositionReturnsNewStopWithUpdatedPosition() {
        CourseStop original = new CourseStop(null, CourseStopTarget.attraction(1L), 3, null, null, null);

        CourseStop updated = original.withPosition(5);

        assertThat(updated.position()).isEqualTo(5);
        assertThat(original.position()).isEqualTo(3);
    }
}
