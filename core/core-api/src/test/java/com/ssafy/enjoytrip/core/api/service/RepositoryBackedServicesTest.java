package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AuthLogMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Tag("service")
class RepositoryBackedServicesTest {

    @Nested
    class MemberServiceEdgeTests {
        private final MemberMapper mapper = mock(MemberMapper.class);
        private final AuthLogMapper authLogMapper = mock(AuthLogMapper.class);
        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        private final MemberService service = new MemberService(passwordEncoder, mapper, authLogMapper);

        @DisplayName("회원가입은 중복 사용자를 등록하지 않는다")
        @Test
        void signupDoesNotInsertDuplicateUser() {
            when(mapper.existsByEmail("ssafy@example.com")).thenReturn(1);

            Member duplicate = new Member(null, "SSAFY", "SSAFY", "ssafy@example.com", "secret", null);

            assertThatThrownBy(() -> service.signup(duplicate))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(USER_ALREADY_EXISTS));

            verify(mapper, never()).insert(any());
        }

        @DisplayName("로그인은 없는 회원과 빈 비밀번호 및 빈 저장 비밀번호를 거부한다")
        @Test
        void loginRejectsMissingMemberBlankPasswordAndBlankStoredPassword() {
            when(mapper.findByEmail("missing")).thenReturn(null);
            when(mapper.findByEmail("blank-input")).thenReturn(new MemberRecord(
                    "A",
                    null,
                    "blank-input@example.com",
                    passwordEncoder.encode("secret"),
                    ""
            ));
            when(mapper.findByEmail("blank-stored")).thenReturn(new MemberRecord(
                    "A", null, "blank-stored@example.com", " ", ""));

            assertThatThrownBy(() -> service.login("missing", "secret"))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));
            assertThatThrownBy(() -> service.login("blank-input", " "))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));
            assertThatThrownBy(() -> service.login("blank-stored", "secret"))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));

            verify(authLogMapper, never()).insert(any());
        }

        @DisplayName("회원 수정은 이메일, 비밀번호, 프로필 이미지를 변경하지 않고 닉네임만 변경한다")
        @Test
        void updateDoesNotChangeEmailAndPassword() {
            MemberRecord record = new MemberRecord(
                    "SSAFY",
                    "기존닉네임",
                    "old@example.com",
                    "old",
                    "https://cdn.example.com/old.png"
            );
            when(mapper.findById(1L)).thenReturn(record);
            when(mapper.update(record)).thenReturn(1);

            service.update(new Member(
                    1L,
                    "Changed Name",
                    "동네핀러",
                    "new@example.com",
                    "new-secret",
                    "https://cdn.example.com/profile.png"
            ));
            assertThat(record.getName()).isEqualTo("SSAFY");
            assertThat(record.getEmail()).isEqualTo("old@example.com");
            assertThat(record.getPassword()).isEqualTo("old");
            assertThat(record.getNickname()).isEqualTo("동네핀러");
            assertThat(record.getProfileImageUrl()).isEqualTo("https://cdn.example.com/old.png");

            service.update(new Member(1L, null, null, null, null, null));
            assertThat(record.getNickname()).isNull();
            assertThat(record.getProfileImageUrl()).isEqualTo("https://cdn.example.com/old.png");

            when(mapper.findById(99L)).thenReturn(null);
            Member missing = new Member(99L, "SSAFY", "SSAFY", "ssafy@example.com", " ", null);

            assertThatThrownBy(() -> service.update(missing))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(USER_NOT_FOUND));
        }
    }
}
