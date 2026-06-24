package com.ssafy.enjoytrip.core.api.web.dto.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ssafy.enjoytrip.core.domain.MapSearchTarget;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MapSearchRequestTest {

    @DisplayName("MapSearchRequest는 유효한 좌표와 키워드가 주어졌을 때 정상 동작하며, trim된 키워드를 반환한다")
    @Test
    void validRequestReturnsExpectedValues() {
        // given
        MapSearchRequest request = new MapSearchRequest("  경복궁  ", 126.9780, 37.5665, 500.0, MapSearchTarget.ALL, null, 10);

        // when & then
        assertThat(request.requiredKeyword()).isEqualTo("경복궁");
        assertThat(request.requiredLongitude()).isEqualTo(126.9780);
        assertThat(request.requiredLatitude()).isEqualTo(37.5665);
        assertThat(request.normalizedTarget()).isEqualTo(MapSearchTarget.ALL);
        assertThat(request.cappedLimit()).isEqualTo(10);
    }

    @DisplayName("MapSearchRequest는 키워드가 null이거나 blank일 때 requiredKeyword() 호출 시 ClientInputException 예외를 반환한다")
    @Test
    void invalidKeywordThrowsException() {
        MapSearchRequest nullKeywordRequest = new MapSearchRequest(null, 126.9780, 37.5665, null, null, null, null);
        MapSearchRequest blankKeywordRequest = new MapSearchRequest("   ", 126.9780, 37.5665, null, null, null, null);

        assertThatThrownBy(nullKeywordRequest::requiredKeyword)
                .isInstanceOf(ClientInputException.class)
                .hasMessage("검색 키워드가 유효하지 않습니다.");

        assertThatThrownBy(blankKeywordRequest::requiredKeyword)
                .isInstanceOf(ClientInputException.class)
                .hasMessage("검색 키워드가 유효하지 않습니다.");
    }

    @DisplayName("MapSearchRequest는 좌표가 누락되었을 때 ClientInputException 예외를 반환한다")
    @Test
    void missingCoordinatesThrowsException() {
        MapSearchRequest missingX = new MapSearchRequest("경복궁", null, 37.5665, null, null, null, null);
        MapSearchRequest missingY = new MapSearchRequest("경복궁", 126.9780, null, null, null, null, null);

        assertThatThrownBy(missingX::requiredLongitude)
                .isInstanceOf(ClientInputException.class)
                .hasMessage("위도 또는 경도가 유효하지 않습니다.");

        assertThatThrownBy(missingY::requiredLatitude)
                .isInstanceOf(ClientInputException.class)
                .hasMessage("위도 또는 경도가 유효하지 않습니다.");
    }

    @DisplayName("MapSearchRequest는 target이 null일 때 기본값 ALL을 반환하며, limit이 null이거나 50보다 크면 cappedLimit이 50을 반환한다")
    @Test
    void targetAndLimitNormalization() {
        MapSearchRequest nullTargetAndLimit = new MapSearchRequest("경복궁", 126.9780, 37.5665, null, null, null, null);
        MapSearchRequest overLimit = new MapSearchRequest("경복궁", 126.9780, 37.5665, null, null, null, 100);

        assertThat(nullTargetAndLimit.normalizedTarget()).isEqualTo(MapSearchTarget.ALL);
        assertThat(nullTargetAndLimit.cappedLimit()).isEqualTo(50);
        assertThat(overLimit.cappedLimit()).isEqualTo(50);
    }
}
