package com.ssafy.enjoytrip.core.support;

import com.ssafy.enjoytrip.core.api.filter.Utf8EncodingFilter;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorCode;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
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
            assertThat(response.getError().message())
                    .isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        @DisplayName("CoreException은 오류 타입과 원인을 보존한다")
        @Test
        void coreExceptionKeepsErrorTypeAndCause() {
            IllegalArgumentException cause = new IllegalArgumentException("잘못된 값입니다.");

            CoreException exception = new CoreException(ErrorType.INVALID_REQUEST, cause);

            assertThat(exception.errorType()).isEqualTo(ErrorType.INVALID_REQUEST);
            assertThat(exception).hasMessage("유효하지 않은 요청입니다.").hasCause(cause);
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
