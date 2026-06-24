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

    @DisplayName("공개 코스 피드는 거리순으로 정렬된다")
    @Test
    void publicFeedOrderedByDistance() {
        Long adminMemberId = seedMember("admin", "admin@example.com");
        Long userMemberId = seedMember("user", "user@example.com");
        jdbcTemplate.update("update members set role = 'ADMIN' where id = ?", adminMemberId);
        seedPublicCourse("admin-far", adminMemberId, ORIGIN_LONGITUDE + 0.0030);
        seedPublicCourse("user-near", userMemberId, ORIGIN_LONGITUDE + 0.0001);
        seedPublicCourse("user-mid", userMemberId, ORIGIN_LONGITUDE + 0.0020);

        List<CourseRecord> feed = courseMapper.findDistanceOrderedPublicFeed(
                ORIGIN_LONGITUDE,
                ORIGIN_LATITUDE,
                5,
                null
        );

        assertThat(feed).extracting(CourseRecord::getId)
                .containsExactly("user-near", "user-mid", "admin-far");
        assertThat(feed).extracting(CourseRecord::getStartLongitude)
                .doesNotContainNull();
        assertThat(feed).extracting(CourseRecord::getDistanceMeters)
                .doesNotContainNull();

        List<CourseRecord> feedWithRadius = courseMapper.findDistanceOrderedPublicFeed(
                ORIGIN_LONGITUDE,
                ORIGIN_LATITUDE,
                5,
                100.0
        );

        assertThat(feedWithRadius).extracting(CourseRecord::getId)
                .containsExactly("user-near");
    }

    private void seedPublicCourse(String id, Long ownerMemberId, double longitude) {
        courseMapper.insert(new CourseRecord(id, ownerMemberId, id, "서울", null));
        assertThat(courseMapper.updateStartLocation(id, longitude, ORIGIN_LATITUDE)).isEqualTo(1);
    }
}
