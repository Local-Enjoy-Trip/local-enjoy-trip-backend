package com.ssafy.enjoytrip.support;

import com.ssafy.enjoytrip.filter.Utf8EncodingFilter;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorCode;
import com.ssafy.enjoytrip.support.error.ErrorType;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import jakarta.servlet.FilterChain;
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
        @Test
        void successResponseCarriesDataWithoutError() {
            ApiResponse<String> response = ApiResponse.success("ok");

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isEqualTo("ok");
            assertThat(response.getError()).isNull();
        }

        @Test
        void failureResponseCopiesStableErrorCodeAndMessage() {
            ApiResponse<Void> response = ApiResponse.fail(ErrorType.INVALID_CREDENTIALS);

            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getData()).isNull();
            assertThat(response.getError().code()).isEqualTo(ErrorCode.UNAUTHORIZED);
            assertThat(response.getError().message()).isEqualTo("Invalid credentials");
        }

        @Test
        void coreExceptionKeepsErrorTypeAndCause() {
            IllegalArgumentException cause = new IllegalArgumentException("bad");

            CoreException exception = new CoreException(ErrorType.INVALID_REQUEST, cause);

            assertThat(exception.errorType()).isEqualTo(ErrorType.INVALID_REQUEST);
            assertThat(exception).hasMessage("Invalid request").hasCause(cause);
        }
    }


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
