package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssafy.enjoytrip.core.domain.BoardPost;
import com.ssafy.enjoytrip.core.domain.Hotplace;
import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.Notice;
import com.ssafy.enjoytrip.core.domain.TravelPlan;
import com.ssafy.enjoytrip.core.domain.auth.PasswordCodec;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.entity.BoardPostEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.HotplaceEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.MemberEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.NoticeEntity;
import com.ssafy.enjoytrip.storage.db.core.entity.TravelPlanEntity;
import com.ssafy.enjoytrip.storage.db.core.jpa.AuthLogJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.BoardPostJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.HotplaceJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.MemberJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.NoticeJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.PlanItemJpaRepository;
import com.ssafy.enjoytrip.storage.db.core.jpa.TravelPlanJpaRepository;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Tag("service")
class RepositoryBackedServicesTest {

    @Nested
    class BoardServiceTests {
        private final BoardPostJpaRepository repository = mock(BoardPostJpaRepository.class);
        private final BoardService service = new BoardService(repository);

        @DisplayName("게시글 등록은 db-core BoardPostEntity를 저장한다")
        @Test
        void savesBoardPostEntity() {
            BoardPost post = new BoardPost("b1", "title", "content", "author", "created", "updated");

            service.insertPost(post);

            verify(repository).save(any(BoardPostEntity.class));
        }
    }

    @Nested
    class HotplaceServiceTests {
        private final HotplaceJpaRepository repository = mock(HotplaceJpaRepository.class);
        private final HotplaceService service = new HotplaceService(repository);

        @DisplayName("핫플레이스 등록과 삭제는 db-core repository를 직접 사용한다")
        @Test
        void savesAndDeletesHotplaceEntity() {
            Hotplace hotplace = new Hotplace("h1", "ssafy", "남산", "view", "2026-05-14", 37.55,
                    126.99, "night", "", "created");
            when(repository.existsById("h1")).thenReturn(true);

            service.insertHotplace(hotplace);
            assertThat(service.deleteHotplace("h1")).isTrue();

            verify(repository).save(any(HotplaceEntity.class));
            verify(repository).deleteById("h1");
        }
    }

    @Nested
    class NoticeServiceTests {
        private final NoticeJpaRepository repository = mock(NoticeJpaRepository.class);
        private final NoticeService service = new NoticeService(repository);

        @DisplayName("공지 수정은 db-core NoticeEntity를 조회한 뒤 변경한다")
        @Test
        void updatesNoticeEntity() {
            Notice notice = new Notice(1L, "공지", "내용", "admin", "created", "updated");
            NoticeEntity entity = new NoticeEntity("기존", "이전", "admin");
            when(repository.findById(1L)).thenReturn(Optional.of(entity));

            assertThat(service.updateNotice(notice)).isTrue();

            assertThat(entity.getTitle()).isEqualTo("공지");
            assertThat(entity.getContent()).isEqualTo("내용");
        }
    }

    @Nested
    class PlanServiceTests {
        private final TravelPlanJpaRepository repository = mock(TravelPlanJpaRepository.class);
        private final PlanItemJpaRepository itemRepository = mock(PlanItemJpaRepository.class);
        private final PlanService service = new PlanService(repository, itemRepository, mock(DSLContext.class));

        @DisplayName("여행 계획 등록은 db-core TravelPlanEntity를 저장한다")
        @Test
        void savesTravelPlanEntity() {
            TravelPlan plan = new TravelPlan("p1", "ssafy", "서울", "2026-05-14", "2026-05-15",
                    1000, "note", "[]", "created");

            service.insertPlan(plan);

            verify(repository).save(any(TravelPlanEntity.class));
        }
    }

    @Nested
    class MemberServiceEdgeTests {
        private final MemberJpaRepository repository = mock(MemberJpaRepository.class);
        private final AuthLogJpaRepository authLogRepository = mock(AuthLogJpaRepository.class);
        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        private final MemberService service = new MemberService(
                new TestPasswordCodec(passwordEncoder),
                repository,
                authLogRepository
        );

        @DisplayName("회원가입은 중복 사용자를 등록하지 않는다")
        @Test
        void signupDoesNotInsertDuplicateUser() {
            when(repository.existsByUserId("ssafy")).thenReturn(true);

            assertThatThrownBy(() -> service.signup(new Member("ssafy", "SSAFY", "ssafy@example.com", "secret", "")))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(USER_ALREADY_EXISTS));

            verify(repository, never()).save(any());
        }

        @DisplayName("로그인은 없는 회원과 빈 비밀번호 및 빈 저장 비밀번호를 거부한다")
        @Test
        void loginRejectsMissingMemberBlankPasswordAndBlankStoredPassword() {
            when(repository.findByUserId("missing")).thenReturn(Optional.empty());
            when(repository.findByUserId("blank-input")).thenReturn(Optional.of(new MemberEntity(
                    "blank-input", "A", null, "a@example.com", passwordEncoder.encode("secret"), "", null, null, null)));
            when(repository.findByUserId("blank-stored")).thenReturn(Optional.of(new MemberEntity(
                    "blank-stored", "A", null, "a@example.com", " ", "", null, null, null)));

            assertThatThrownBy(() -> service.login("missing", "secret"))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));
            assertThatThrownBy(() -> service.login("blank-input", " "))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));
            assertThatThrownBy(() -> service.login("blank-stored", "secret"))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(INVALID_CREDENTIALS));

            verify(authLogRepository, never()).save(any());
        }

        @DisplayName("회원 수정은 값이 있는 비밀번호만 인코딩하고 빈 비밀번호는 비워 둔다")
        @Test
        void updateEncodesNonBlankPasswordAndLeavesBlankPasswordBlank() {
            MemberEntity entity = new MemberEntity("ssafy", "SSAFY", null, "old@example.com", "old", "",
                    null, null, null);
            when(repository.findByUserId("ssafy")).thenReturn(Optional.of(entity));

            service.update(new Member("ssafy", "SSAFY", "ssafy@example.com", "new-secret", ""));
            assertThat(passwordEncoder.matches("new-secret", entity.getPassword())).isTrue();

            when(repository.findByUserId("missing")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.update(new Member("missing", "SSAFY", "ssafy@example.com", " ", "")))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(USER_NOT_FOUND));
        }

        @DisplayName("로그아웃과 비밀번호 조회 및 삭제는 db-core repository를 직접 사용한다")
        @Test
        void delegatesLogoutPasswordLookupAndDelete() {
            MemberEntity entity = new MemberEntity("ssafy", "SSAFY", null, "ssafy@example.com", "legacy-secret",
                    "", null, null, null);
            when(repository.findByUserIdAndEmail("ssafy", "ssafy@example.com")).thenReturn(Optional.of(entity));
            when(repository.existsByUserId("ssafy")).thenReturn(true);

            service.logout("ssafy");

            verify(authLogRepository).save(any());
            assertThat(service.findPassword("ssafy", "ssafy@example.com")).isEqualTo("legacy-secret");
            service.delete("ssafy");
            verify(repository).deleteByUserId("ssafy");
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
