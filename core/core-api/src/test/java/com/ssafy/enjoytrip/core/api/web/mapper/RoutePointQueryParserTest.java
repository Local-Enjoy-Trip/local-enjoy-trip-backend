package com.ssafy.enjoytrip.core.api.web.mapper;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_POINTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ssafy.enjoytrip.core.domain.Point;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoutePointQueryParserTest {

    @DisplayName("경로 쿼리 파서는 파이프 구분 좌표를 입력 순서 인덱스로 변환한다")
    @Test
    void parsesPipeDelimitedLatitudeLongitudePairsWithOriginalIndexes() {
        List<Point> points = RoutePointQueryParser.parse(
                "37.5665,126.9780 | 35.1796,129.0756",
                INVALID_POINTS
        );

        assertThat(points).containsExactly(
                new Point(37.5665, 126.9780, 0),
                new Point(35.1796, 129.0756, 1)
        );
    }

    @DisplayName("경로 쿼리 파서는 빈 입력은 빈 목록으로 바꾸고 잘못된 좌표는 거부한다")
    @Test
    void returnsEmptyListForMissingInputAndRejectsMalformedCoordinates() {
        assertThat(RoutePointQueryParser.parse(null, INVALID_POINTS)).isEmpty();
        assertThat(RoutePointQueryParser.parse(" ", INVALID_POINTS)).isEmpty();

        assertThatThrownBy(() -> RoutePointQueryParser.parse("37.5|bad,126.9", INVALID_POINTS))
                .isInstanceOfSatisfying(CoreException.class,
                        exception -> assertThat(exception.errorType()).isEqualTo(INVALID_POINTS));
    }
}
