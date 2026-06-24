package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.CourseBriefingCandidateRecord;
import com.ssafy.enjoytrip.storage.db.core.model.NewsItemRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NeighborhoodBriefingMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NewsMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NewsBriefingMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private NeighborhoodBriefingMapper neighborhoodBriefingMapper;

    @DisplayName("NewsMapper는 H2 인메모리 DB에서 최신 뉴스 목록을 조회한다")
    @Test
    void newsMapperReadsLatestRows() {
        String firstNewsId = uniqueId("news-first");
        String latestNewsId = uniqueId("news-latest");
        jdbcTemplate.update("""
                insert into news_items (id, title, link, summary, source, published_at, created_at)
                values (
                    ?,
                    '과거 뉴스',
                    'https://example.com/old',
                    '요약',
                    'test',
                    '2026-06-18',
                    timestamp '2026-06-18 00:00:00'
                )
                """, firstNewsId);
        jdbcTemplate.update("""
                insert into news_items (id, title, link, summary, source, published_at, created_at)
                values (
                    ?,
                    '최신 뉴스',
                    'https://example.com/new',
                    '요약',
                    'test',
                    '2026-06-19',
                    timestamp '2026-06-19 00:00:00'
                )
                """, latestNewsId);

        List<NewsItemRecord> news = newsMapper.findLatest(10);

        assertThat(news).extracting(NewsItemRecord::id).contains(latestNewsId, firstNewsId);
        assertThat(news.getFirst().id()).isEqualTo(latestNewsId);
    }

    @DisplayName("NeighborhoodBriefingMapper는 H2에서 공개 READY 코스를 지역 우선으로 조회한다")
    @Test
    void neighborhoodBriefingMapperFindsPublicReadyCourses() {
        Long ownerMemberId = seedMember("course-owner", uniqueId("course-owner") + "@example.com");
        String localCourseId = uniqueId("course-local");
        String otherCourseId = uniqueId("course-other");
        jdbcTemplate.update("""
                insert into courses (id, owner_member_id, title, region_name, created_at)
                values (?, ?, '지역 코스', '서울 중구', timestamp '2026-06-18 00:00:00')
                """, localCourseId, ownerMemberId);
        jdbcTemplate.update("""
                insert into courses (id, owner_member_id, title, region_name, created_at)
                values (?, ?, '다른 지역 코스', '부산 해운대구', timestamp '2026-06-19 00:00:00')
                """, otherCourseId, ownerMemberId);

        List<CourseBriefingCandidateRecord> candidates =
                neighborhoodBriefingMapper.findPublicReadyCandidates("서울 중구", 10);

        assertThat(candidates).extracting(CourseBriefingCandidateRecord::id)
                .contains(localCourseId, otherCourseId);
        assertThat(candidates.getFirst().id()).isEqualTo(localCourseId);
    }
}
