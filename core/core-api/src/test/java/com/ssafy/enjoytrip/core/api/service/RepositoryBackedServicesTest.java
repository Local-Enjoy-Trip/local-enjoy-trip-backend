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

import com.ssafy.enjoytrip.core.domain.BoardPost;
import com.ssafy.enjoytrip.core.domain.Hotplace;
import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.Notice;
import com.ssafy.enjoytrip.core.domain.PlanItem;
import com.ssafy.enjoytrip.core.domain.TravelPlan;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import com.ssafy.enjoytrip.storage.db.core.model.HotplaceRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoticeRecord;
import com.ssafy.enjoytrip.storage.db.core.model.PlanItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.TravelPlanRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AuthLogMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.BoardPostMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.HotplaceMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoticeMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.PlanMapper;
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
    class BoardServiceTests {
        private final BoardPostMapper mapper = mock(BoardPostMapper.class);
        private final BoardService service = new BoardService(mapper);

        @DisplayName("게시글 등록은 db-core BoardPostRecord를 MyBatis mapper로 저장한다")
        @Test
        void savesBoardPostRecord() {
            BoardPost post = new BoardPost("b1", "title", "content", "author", "created", "updated");

            service.insertPost(post);

            verify(mapper).insert(any());
        }
    }

    @Nested
    class HotplaceServiceTests {
        private final HotplaceMapper mapper = mock(HotplaceMapper.class);
        private final HotplaceService service = new HotplaceService(mapper);

        @DisplayName("핫플레이스 등록과 삭제는 MyBatis mapper를 사용한다")
        @Test
        void savesAndDeletesHotplaceRecord() {
            Hotplace hotplace = new Hotplace("h1", "ssafy", "남산", "view", "2026-05-14", 37.55,
                    126.99, "night", "", "created");
            when(mapper.existsById("h1")).thenReturn(1);
            when(mapper.deleteById("h1")).thenReturn(1);

            service.insertHotplace(hotplace);
            assertThat(service.deleteHotplace("h1")).isTrue();

            verify(mapper).insert(any(HotplaceRecord.class));
            verify(mapper).deleteById("h1");
        }
    }

    @Nested
    class NoticeServiceTests {
        private final NoticeMapper mapper = mock(NoticeMapper.class);
        private final NoticeService service = new NoticeService(mapper);

        @DisplayName("공지 수정은 db-core NoticeRecord를 조회한 뒤 mapper로 갱신한다")
        @Test
        void updatesNoticeRecord() {
            Notice notice = new Notice(1L, "공지", "내용", "admin", "created", "updated");
            NoticeRecord record = new NoticeRecord("기존", "이전", "admin");
            when(mapper.findById(1L)).thenReturn(record);
            when(mapper.update(record)).thenReturn(1);

            assertThat(service.updateNotice(notice)).isTrue();

            assertThat(record.getTitle()).isEqualTo("공지");
            assertThat(record.getContent()).isEqualTo("내용");
        }
    }

    @Nested
    class PlanServiceTests {
        private final PlanMapper mapper = mock(PlanMapper.class);
        private final PlanService service = new PlanService(mapper);

        @DisplayName("여행 계획 등록은 db-core TravelPlanRecord를 MyBatis mapper로 저장한다")
        @Test
        void savesTravelPlanRecord() {
            TravelPlan plan = new TravelPlan("p1", "ssafy", "서울", "2026-05-14", "2026-05-15",
                    1000, "note", "[]", "created");

            service.insertPlan(plan);

            verify(mapper).insertPlan(any(TravelPlanRecord.class));
        }

        @DisplayName("여행 계획 코스 저장은 관광지 좌표 기준으로 같은 일자 안에서 순서를 최적화한다")
        @Test
        void savesPlanItemsInOptimizedOrderByAttractionCoordinates() {
            TravelPlan plan = new TravelPlan("p1", "ssafy", "서울", "2026-05-14", "2026-05-15",
                    1000, "note", "[]", "created");
            List<PlanItem> items = List.of(
                    new PlanItem(null, "p1", 1L, 0, 1, "start", 90),
                    new PlanItem(null, "p1", 2L, 0, 1, "far", 90),
                    new PlanItem(null, "p1", 3L, 0, 1, "near", 90)
            );
            when(mapper.existsById("p1")).thenReturn(1);
            when(mapper.findAttractionsByIds(List.of(1L, 2L, 3L))).thenReturn(List.of(
                    attraction(1L, 37.5665, 126.9780),
                    attraction(2L, 35.1796, 129.0756),
                    attraction(3L, 37.5700, 126.9820)
            ));

            service.insertPlan(plan, items);

            ArgumentCaptor<PlanItemRecord> itemCaptor = ArgumentCaptor.forClass(PlanItemRecord.class);
            verify(mapper, times(3)).insertItem(itemCaptor.capture());
            assertThat(itemCaptor.getAllValues())
                    .extracting(PlanItemRecord::getAttractionId)
                    .containsExactly(1L, 3L, 2L);
            assertThat(itemCaptor.getAllValues())
                    .extracting(PlanItemRecord::getPosition)
                    .containsExactly(1, 2, 3);
        }

        private AttractionRecord attraction(Long id, Double latitude, Double longitude) {
            return new AttractionRecord(
                    id,
                    "관광지 " + id,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    0,
                    1,
                    1,
                    latitude,
                    longitude,
                    "",
                    "",
                    ""
            );
        }
    }

    @Nested
    class MemberServiceEdgeTests {
        private final MemberMapper mapper = mock(MemberMapper.class);
        private final AuthLogMapper authLogMapper = mock(AuthLogMapper.class);
        private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        private final MemberService service = new MemberService(passwordEncoder, mapper, authLogMapper);

        @DisplayName("회원가입은 중복 사용자를 등록하지 않는다")
        @Test
        void signupDoesNotInsertDuplicateUser() {
            when(mapper.existsByUserIdOrEmail("ssafy", "ssafy@example.com")).thenReturn(1);

            Member duplicate = new Member("ssafy", "SSAFY", "ssafy@example.com", "secret");

            assertThatThrownBy(() -> service.signup(duplicate))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(USER_ALREADY_EXISTS));

            verify(mapper, never()).insert(any());
        }

        @DisplayName("로그인은 없는 회원과 빈 비밀번호 및 빈 저장 비밀번호를 거부한다")
        @Test
        void loginRejectsMissingMemberBlankPasswordAndBlankStoredPassword() {
            when(mapper.findByUserId("missing")).thenReturn(null);
            when(mapper.findByUserId("blank-input")).thenReturn(new MemberRecord(
                    "blank-input",
                    "A",
                    null,
                    "a@example.com",
                    passwordEncoder.encode("secret"),
                    ""
            ));
            when(mapper.findByUserId("blank-stored")).thenReturn(new MemberRecord(
                    "blank-stored", "A", null, "a@example.com", " ", ""));

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
                    "ssafy",
                    "SSAFY",
                    "기존닉네임",
                    "old@example.com",
                    "old",
                    "https://cdn.example.com/old.png"
            );
            when(mapper.findByUserId("ssafy")).thenReturn(record);
            when(mapper.update(record)).thenReturn(1);

            service.update(new Member(
                    "ssafy",
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

            service.update(new Member("ssafy", null, null, null, null, null));
            assertThat(record.getNickname()).isNull();
            assertThat(record.getProfileImageUrl()).isEqualTo("https://cdn.example.com/old.png");

            when(mapper.findByUserId("missing")).thenReturn(null);
            Member missing = new Member("missing", "SSAFY", "ssafy@example.com", " ");

            assertThatThrownBy(() -> service.update(missing))
                    .isInstanceOfSatisfying(CoreException.class,
                            exception -> assertThat(exception.errorType()).isEqualTo(USER_NOT_FOUND));
        }
    }
}
