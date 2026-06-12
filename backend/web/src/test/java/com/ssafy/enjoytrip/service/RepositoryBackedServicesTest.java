package com.ssafy.enjoytrip.service;

import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.support.error.ErrorType.USER_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.support.error.ErrorType.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.domain.Hotplace;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.domain.Notice;
import com.ssafy.enjoytrip.domain.TravelPlan;
import com.ssafy.enjoytrip.repository.BoardRepository;
import com.ssafy.enjoytrip.repository.HotplaceRepository;
import com.ssafy.enjoytrip.repository.MemberRepository;
import com.ssafy.enjoytrip.repository.NoticeRepository;
import com.ssafy.enjoytrip.repository.PlanRepository;
import com.ssafy.enjoytrip.security.PasswordCodec;
import com.ssafy.enjoytrip.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Tag("service")
class RepositoryBackedServicesTest {

    @Nested
    class BoardServiceTests {
        private final BoardRepository repository = mock(BoardRepository.class);
        private final BoardService service = new BoardService(repository);

        @DisplayName("CRUD 작업을 저장소에 위임한다")
        @Test
        void delegatesCrudToRepository() {
            BoardPost post = new BoardPost("b1", "title", "content", "author", "created", "updated");
            when(repository.findAll()).thenReturn(List.of(post));
            when(repository.update(post)).thenReturn(true);
            when(repository.delete("b1")).thenReturn(true);

            assertThat(service.findAllPosts()).containsExactly(post);
            service.insertPost(post);
            assertThat(service.updatePost(post)).isTrue();
            assertThat(service.deletePost("b1")).isTrue();

            verify(repository).insert(post);
        }

        @DisplayName("저장소 예외를 그대로 전파한다")
        @Test
        void propagatesRepositoryException() {
            BoardPost post = new BoardPost("b1", "title", "content", "author", "", "");
            doThrow(new IllegalStateException("repository down")).when(repository).insert(post);

            assertThatThrownBy(() -> service.insertPost(post))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("repository down");
        }
    }

    @Nested
    class HotplaceServiceTests {
        private final HotplaceRepository repository = mock(HotplaceRepository.class);
        private final HotplaceService service = new HotplaceService(repository);

        @DisplayName("목록 조회와 등록 및 삭제를 저장소에 위임한다")
        @Test
        void delegatesListInsertAndDelete() {
            Hotplace hotplace = new Hotplace("h1", "ssafy", "남산", "view", "2026-05-14", 37.55, 126.99, "night", "", "created");
            when(repository.findAll()).thenReturn(List.of(hotplace));
            when(repository.findByUser("ssafy")).thenReturn(List.of(hotplace));
            when(repository.delete("h1")).thenReturn(true);

            assertThat(service.findAllHotplaces()).containsExactly(hotplace);
            assertThat(service.findHotplacesByUser("ssafy")).containsExactly(hotplace);
            service.insertHotplace(hotplace);
            assertThat(service.deleteHotplace("h1")).isTrue();

            verify(repository).insert(hotplace);
        }
    }

    @Nested
    class NoticeServiceTests {
        private final NoticeRepository repository = mock(NoticeRepository.class);
        private final NoticeService service = new NoticeService(repository);

        @DisplayName("공지 CRUD를 위임하고 없는 공지는 false를 반환한다")
        @Test
        void delegatesCrudAndReturnsFalseForMissingNotice() {
            Notice notice = new Notice(1L, "공지", "내용", "admin", "created", "updated");
            when(repository.findAll()).thenReturn(List.of(notice));
            when(repository.update(notice)).thenReturn(false);
            when(repository.delete(1L)).thenReturn(false);

            assertThat(service.findAllNotices()).containsExactly(notice);
            service.insertNotice(notice);
            assertThat(service.updateNotice(notice)).isFalse();
            assertThat(service.deleteNotice(1L)).isFalse();

            verify(repository).insert(notice);
        }
    }

    @Nested
    class PlanServiceTests {
        private final PlanRepository repository = mock(PlanRepository.class);
        private final PlanService service = new PlanService(repository);

        @DisplayName("전체 또는 사용자별 조회와 삭제를 저장소에 위임한다")
        @Test
        void delegatesReadByAllOrUserAndDelete() {
            TravelPlan plan = new TravelPlan("p1", "ssafy", "서울", "2026-05-14", "2026-05-15", 1000, "note", "[]", "created");
            when(repository.findAll()).thenReturn(List.of(plan));
            when(repository.findByUser("ssafy")).thenReturn(List.of(plan));
            when(repository.delete("p1")).thenReturn(true);

            assertThat(service.findAllPlans()).containsExactly(plan);
            assertThat(service.findPlansByUser("ssafy")).containsExactly(plan);
            service.insertPlan(plan);
            assertThat(service.deletePlan("p1")).isTrue();

            verify(repository).insert(plan);
        }
    }

    @Nested
    class MemberServiceEdgeTests {
        private final MemberRepository repository = mock(MemberRepository.class);
        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        private final MemberService service = new MemberService(repository, new TestPasswordCodec(passwordEncoder));

        @DisplayName("회원가입은 중복 사용자를 등록하지 않는다")
        @Test
        void signupDoesNotInsertDuplicateUser() {
            when(repository.existsByUserId("ssafy")).thenReturn(true);

            assertThatThrownBy(() -> service.signup(new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", "")))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(USER_ALREADY_EXISTS));

            verify(repository, never()).insert(org.mockito.ArgumentMatchers.any());
        }

        @DisplayName("로그인은 없는 회원과 빈 비밀번호 및 빈 저장 비밀번호를 거부한다")
        @Test
        void loginRejectsMissingMemberBlankPasswordAndBlankStoredPassword() {
            when(repository.findByUserId("missing")).thenReturn(null);
            when(repository.findByUserId("blank-input")).thenReturn(new Member("blank-input", "A", "a@example.com", passwordEncoder.encode("secret"), ""));
            when(repository.findByUserId("blank-stored")).thenReturn(new Member("blank-stored", "A", "a@example.com", " ", ""));

            assertThatThrownBy(() -> service.login("missing", "secret"))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));
            assertThatThrownBy(() -> service.login("blank-input", " "))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));
            assertThatThrownBy(() -> service.login("blank-stored", "secret"))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));

            verify(repository, never()).insertAuthLog(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @DisplayName("회원 수정은 값이 있는 비밀번호만 인코딩하고 빈 비밀번호는 비워 둔다")
        @Test
        void updateEncodesNonBlankPasswordAndLeavesBlankPasswordBlank() {
            when(repository.update(org.mockito.ArgumentMatchers.any())).thenReturn(true);

            service.update(new Member("ssafy", "SSAFY", "ssafy@example.com", "new-secret", ""));
            ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
            verify(repository).update(captor.capture());
            assertThat(passwordEncoder.matches("new-secret", captor.getValue().password())).isTrue();

            when(repository.update(org.mockito.ArgumentMatchers.any())).thenReturn(false);
            assertThatThrownBy(() -> service.update(new Member("ssafy", "SSAFY", "ssafy@example.com", " ", "")))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(USER_NOT_FOUND));
            verify(repository, org.mockito.Mockito.times(2)).update(captor.capture());
            assertThat(captor.getAllValues().getLast().password()).isEqualTo(" ");
        }

        @DisplayName("로그아웃과 비밀번호 조회 및 삭제를 저장소에 위임한다")
        @Test
        void delegatesLogoutPasswordLookupAndDelete() {
            when(repository.findPassword("ssafy", "ssafy@example.com")).thenReturn("legacy-secret");
            when(repository.delete("ssafy")).thenReturn(true);

            service.logout("ssafy");

            verify(repository).insertAuthLog("ssafy", "LOGOUT");
            assertThat(service.findPassword("ssafy", "ssafy@example.com")).isEqualTo("legacy-secret");
            service.delete("ssafy");
            verify(repository).delete("ssafy");
        }
    }

    private record TestPasswordCodec(PasswordEncoder passwordEncoder) implements PasswordCodec {
        @Override
        public String encode(String rawPassword) {
            return passwordEncoder.encode(rawPassword);
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }

        @Override
        public boolean isEncoded(String password) {
            return password != null && password.startsWith("$2");
        }
    }
}
