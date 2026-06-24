package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.BoardPostRecord;
import com.ssafy.enjoytrip.storage.db.core.model.HotplaceRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NoticeRecord;
import com.ssafy.enjoytrip.storage.db.core.model.PlanItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.TravelPlanRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.BoardPostMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.HotplaceMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoticeMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.PlanMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommunityPlanMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private BoardPostMapper boardPostMapper;

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private HotplaceMapper hotplaceMapper;

    @Autowired
    private PlanMapper planMapper;

    @DisplayName("BoardPostMapper는 H2에서 게시글 CRUD SQL을 실행한다")
    @Test
    void boardPostMapperPersistsAndMutatesBoardPost() {
        String id = uniqueId("board");
        BoardPostRecord record = new BoardPostRecord(id, "게시글", "본문", "admin");

        boardPostMapper.insert(record);
        record.update("게시글 수정", "본문 수정");
        boardPostMapper.update(record);

        BoardPostRecord updated = boardPostMapper.findById(id);

        assertThat(boardPostMapper.existsById(id)).isEqualTo(1);
        assertThat(updated.getTitle()).isEqualTo("게시글 수정");
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(boardPostMapper.findAllOrderByCreatedAtDesc())
                .extracting(BoardPostRecord::getId)
                .contains(id);
        assertThat(boardPostMapper.deleteById(id)).isEqualTo(1);
    }

    @DisplayName("NoticeMapper는 H2 인메모리 DB에서 공지 생성, 조회, 수정, 삭제 SQL을 실행한다")
    @Test
    void noticeMapperPersistsAndMutatesNotice() {
        NoticeRecord record = new NoticeRecord("공지", "내용", "admin");

        noticeMapper.insert(record);
        record.update("공지 수정", "내용 수정");
        noticeMapper.update(record);

        NoticeRecord updated = noticeMapper.findById(record.getId());

        assertThat(record.getId()).isNotNull();
        assertThat(noticeMapper.existsById(record.getId())).isEqualTo(1);
        assertThat(updated.getTitle()).isEqualTo("공지 수정");
        assertThat(noticeMapper.findAllOrderByCreatedAtDesc())
                .extracting(NoticeRecord::getId)
                .contains(record.getId());
        assertThat(noticeMapper.deleteById(record.getId())).isEqualTo(1);
    }

    @DisplayName("HotplaceMapper는 H2에서 회원별 핫플레이스 SQL을 실행한다")
    @Test
    void hotplaceMapperPersistsAndFindsUserHotplace() {
        Long memberId = seedMember("hotplace-member", uniqueId("hotplace") + "@example.com");
        String id = uniqueId("hotplace");
        HotplaceRecord record = new HotplaceRecord(
                id,
                memberId,
                "핫플",
                "CAFE",
                "2026-06-19",
                37.5665,
                126.9780,
                "좋았음",
                "photo.png"
        );

        hotplaceMapper.insert(record);

        assertThat(hotplaceMapper.existsById(id)).isEqualTo(1);
        assertThat(hotplaceMapper.findByMemberIdOrderByCreatedAtDesc(memberId))
                .extracting(HotplaceRecord::getId)
                .contains(id);
        assertThat(hotplaceMapper.findAllOrderByCreatedAtDesc())
                .extracting(HotplaceRecord::getId)
                .contains(id);
        assertThat(hotplaceMapper.deleteByIdAndMemberId(id, memberId)).isEqualTo(1);
    }

    @DisplayName("PlanMapper는 H2 인메모리 DB에서 여행 계획과 경유지 item SQL을 실행한다")
    @Test
    void planMapperPersistsPlanAndItems() {
        String planId = uniqueId("plan");
        long attractionId = 9100001L;
        Long memberId = seedMember("planner", uniqueId("planner") + "@example.com");
        TravelPlanRecord plan = new TravelPlanRecord(
                planId,
                memberId,
                "서울 여행",
                "2026-06-19",
                "2026-06-20",
                100000,
                "메모",
                "[]"
        );

        planMapper.insertPlan(plan);
        PlanItemRecord item = new PlanItemRecord(planId, attractionId, 1, 1, "첫 코스", 90);
        planMapper.insertItem(item);
        plan.update("서울 여행 수정", "2026-06-20", "2026-06-21", 120000, "메모 수정", "[]");
        planMapper.updatePlan(plan);

        assertThat(planMapper.existsById(planId)).isEqualTo(1);
        assertThat(planMapper.findById(planId).getTitle()).isEqualTo("서울 여행 수정");
        assertThat(planMapper.findByMemberIdOrderByCreatedAtDesc(memberId))
                .extracting(TravelPlanRecord::getId)
                .contains(planId);
        assertThat(planMapper.findItemsByPlanIdOrderByPositionAsc(planId))
                .extracting(PlanItemRecord::getId)
                .contains(item.getId());
        assertThat(planMapper.findItemById(item.getId()).getMemo()).isEqualTo("첫 코스");
        assertThat(planMapper.deleteItemById(item.getId())).isEqualTo(1);
        assertThat(planMapper.deletePlanById(planId)).isEqualTo(1);
    }
}
