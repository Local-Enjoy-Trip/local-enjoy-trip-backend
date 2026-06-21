package com.ssafy.enjoytrip.core.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorCode;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
            assertThat(response.getError().code()).isEqualTo("M004");
            assertThat(response.getError().message())
                    .isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        @DisplayName("CoreException은 오류 타입과 원인을 보존한다")
        @Test
        void coreExceptionKeepsErrorTypeAndCause() {
            IllegalArgumentException cause = new IllegalArgumentException("잘못된 값입니다.");

            CoreException exception = new CoreException(ErrorType.USER_NOT_FOUND, cause);

            assertThat(exception.errorType()).isEqualTo(ErrorType.USER_NOT_FOUND);
            assertThat(exception).hasMessage("사용자를 찾을 수 없습니다.").hasCause(cause);
        }

        @DisplayName("비즈니스 오류 코드는 도메인 prefix와 고유성을 지킨다")
        @Test
        void businessErrorCodesUseDomainPrefixesAndStayUnique() {
            Set<String> allowedPrefixes = Set.of("M", "A", "P", "F", "N", "B", "H");
            List<String> codes = Arrays.stream(ErrorType.values())
                    .map(ErrorType::code)
                    .map(Enum::toString)
                    .toList();

            assertThat(codes).doesNotHaveDuplicates();
            assertThat(codes).allSatisfy(code -> {
                assertThat(allowedPrefixes).contains(code.substring(0, 1));
                assertThat(code.substring(1)).matches("\\d{3}");
            });
        }

        @DisplayName("오류 코드 enum은 코드값 상수만 담고 고유성을 지킨다")
        @Test
        void errorCodeEnumContainsOnlyStableCodeValuesAndStayUnique() {
            Set<String> allowedPrefixes = Set.of("M", "A", "P", "F", "N", "B", "H", "C", "S", "X", "I");
            List<String> codes = Arrays.stream(ErrorCode.values())
                    .map(Enum::toString)
                    .toList();

            assertThat(codes).doesNotHaveDuplicates();
            assertThat(codes).allSatisfy(code -> {
                assertThat(code).matches("[A-Z]\\d{3}");
                assertThat(allowedPrefixes).contains(code.substring(0, 1));
            });
        }
    }
}
