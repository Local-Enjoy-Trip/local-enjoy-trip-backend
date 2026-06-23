package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRouteSegmentRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CourseMapper {
    int insert(CourseRecord record);

    CourseRecord findById(String id);

    List<CourseRecord> findByOwnerMemberId(Long ownerMemberId);

    List<CourseRecord> findAdminOwned();

    int updateOwned(CourseRecord record);

    int updateStartLocation(@Param("id") String id,
                            @Param("longitude") Double longitude,
                            @Param("latitude") Double latitude);

    int softDeleteOwned(@Param("id") String id, @Param("ownerMemberId") Long ownerMemberId);

    int deleteSegmentsByCourseId(String courseId);

    int deleteItemsByCourseId(String courseId);

    int insertItem(CourseItemRecord record);

    int insertItems(List<CourseItemRecord> records);

    List<CourseItemRecord> findItemIdsByCourseId(String courseId);

    int insertSegment(CourseRouteSegmentRecord record);

    int insertSegments(List<CourseRouteSegmentRecord> records);

    List<CourseRouteSegmentRecord> findSegmentsByCourseId(String courseId);

    List<CourseItemDetailRecord> findItemsByCourseId(String courseId);

    List<CourseItemDetailRecord> findPublicItemsByCourseId(String courseId);

    List<CourseRecord> findDistanceOrderedPublicFeed(@Param("longitude") double longitude,
                                                      @Param("latitude") double latitude,
                                                      @Param("limit") int limit,
                                                      @Param("radiusMeters") Double radiusMeters);
}
