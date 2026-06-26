package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void clearCourses() {
        jdbcTemplate.update("delete from course_saves");
        jdbcTemplate.update("delete from courses");
    }

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

    @DisplayName("코스 피드는 동네 이름 앞 2글자 매칭 및 저장수 정렬 기준으로 필터링된다")
    @Test
    void regionPrefixFeedAndOrderedBySaveCount() {
        Long userMemberId = seedMember("user", "user@example.com");
        Long otherUserMemberId = seedMember("other", "other@example.com");

        courseMapper.insert(new CourseRecord("c1", userMemberId, "망원 1", "망원동", null));
        courseMapper.insert(new CourseRecord("c2", userMemberId, "망원 2", "망원동", null));
        courseMapper.insert(new CourseRecord("c3", userMemberId, "부산 1", "부산 해운대구", null));
        courseMapper.insert(new CourseRecord("c4", userMemberId, "망원 3", "망원동", null));

        courseMapper.insertSave("c1", userMemberId);
        courseMapper.insertSave("c1", otherUserMemberId);

        courseMapper.insertSave("c2", userMemberId);

        courseMapper.insertSave("c3", userMemberId);

        List<CourseRecord> feed = courseMapper.findByRegionPrefixOrderedBySaveCount("망원", 5);

        assertThat(feed).extracting(CourseRecord::getId)
                .containsExactly("c1", "c2", "c4");
    }

    private void seedPublicCourse(String id, Long ownerMemberId, double longitude) {
        courseMapper.insert(new CourseRecord(id, ownerMemberId, id, "망원동", null));
        assertThat(courseMapper.updateStartLocation(id, longitude, ORIGIN_LATITUDE)).isEqualTo(1);
    }
}
