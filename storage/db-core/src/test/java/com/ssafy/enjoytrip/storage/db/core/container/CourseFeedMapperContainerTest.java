package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("postgis")
class CourseFeedMapperContainerTest extends StorageContainerTestSupport {
    private static final double ORIGIN_LONGITUDE = 126.9780;
    private static final double ORIGIN_LATITUDE = 37.5665;

    @Autowired
    private CourseMapper courseMapper;

    @DisplayName("공개 코스 피드는 가까운 MD 추천 3개를 먼저 두고 나머지를 거리순으로 채운다")
    @Test
    void publicFeedUsesStartLocationKnnWithMdPriority() {
        seedMember("admin", "admin@example.com");
        seedMember("user", "user@example.com");
        jdbcTemplate.update("update members set role = 'ADMIN' where user_id = 'admin'");
        seedPublicCourse("md-1", "admin", "MD_RECOMMENDED", ORIGIN_LONGITUDE + 0.0010);
        seedPublicCourse("md-2", "admin", "MD_RECOMMENDED", ORIGIN_LONGITUDE + 0.0020);
        seedPublicCourse("md-3", "admin", "MD_RECOMMENDED", ORIGIN_LONGITUDE + 0.0030);
        seedPublicCourse("md-4", "admin", "MD_RECOMMENDED", ORIGIN_LONGITUDE + 0.0040);
        seedPublicCourse("near-user", "user", null, ORIGIN_LONGITUDE + 0.0001);
        seedPublicCourse("mid-user", "user", null, ORIGIN_LONGITUDE + 0.0025);

        List<CourseRecord> feed = courseMapper.findDistanceOrderedPublicFeed(
                ORIGIN_LONGITUDE,
                ORIGIN_LATITUDE,
                5,
                300.0
        );

        assertThat(feed).extracting(CourseRecord::getId)
                .containsExactly("md-1", "md-2", "md-3", "near-user", "mid-user");
        assertThat(feed).extracting(CourseRecord::getStartLongitude)
                .doesNotContainNull();
        assertThat(feed).extracting(CourseRecord::getStartLatitude)
                .containsOnly(ORIGIN_LATITUDE);
        assertThat(feed).extracting(CourseRecord::getDistanceMeters)
                .doesNotContainNull();
        assertThat(feed).extracting(CourseRecord::getCreatedByAdmin)
                .containsExactly(true, true, true, false, false);

        List<CourseRecord> feedWithoutRadius = courseMapper.findDistanceOrderedPublicFeed(
                ORIGIN_LONGITUDE,
                ORIGIN_LATITUDE,
                4,
                null
        );

        assertThat(feedWithoutRadius).extracting(CourseRecord::getId)
                .containsExactly("md-1", "md-2", "md-3", "near-user");
    }

    private void seedPublicCourse(String id,
                                  String ownerUserId,
                                  String curationSection,
                                  double longitude) {
        courseMapper.insert(new CourseRecord(
                id,
                ownerUserId,
                id,
                "서울",
                "PUBLIC",
                "READY",
                null,
                null,
                curationSection,
                null
        ));
        assertThat(courseMapper.updateStartLocation(id, longitude, ORIGIN_LATITUDE)).isEqualTo(1);
    }
}
