package com.ssafy.enjoytrip.support;

import com.ssafy.enjoytrip.filter.Utf8EncodingFilter;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorCode;
import com.ssafy.enjoytrip.support.error.ErrorType;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Tag("support")
class SupportUtilitiesTest {

    @Nested
    class ApiResponsesAndErrors {
        @DisplayName("성공 응답은 오류 없이 데이터를 담는다")
        @Test
        void successResponseCarriesDataWithoutError() {
            ApiResponse<String> response = ApiResponse.success("ok");

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo("ok");
            assertThat(response.getError()).isNull();
        }

        @DisplayName("실패 응답은 안정적인 오류 코드와 메시지를 복사한다")
        @Test
        void failureResponseCopiesStableErrorCodeAndMessage() {
            ApiResponse<Void> response = ApiResponse.fail(ErrorType.INVALID_CREDENTIALS);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getData()).isNull();
            assertThat(response.getError().code()).isEqualTo(ErrorCode.UNAUTHORIZED);
            assertThat(response.getError().message()).isEqualTo("Invalid credentials");
        }

        @DisplayName("CoreException은 오류 타입과 원인을 보존한다")
        @Test
        void coreExceptionKeepsErrorTypeAndCause() {
            IllegalArgumentException cause = new IllegalArgumentException("bad");

            CoreException exception = new CoreException(ErrorType.INVALID_REQUEST, cause);

            assertThat(exception.errorType()).isEqualTo(ErrorType.INVALID_REQUEST);
            assertThat(exception).hasMessage("Invalid request").hasCause(cause);
        }
    }


    @DisplayName("UTF-8 필터는 다음 처리 전에 요청과 응답 인코딩을 설정한다")
    @Test
    void utf8EncodingFilterSetsRequestAndResponseEncodingBeforeContinuing() throws Exception {
        Utf8EncodingFilter filter = new Utf8EncodingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(request.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        verify(chain).doFilter(request, response);
    }
}
