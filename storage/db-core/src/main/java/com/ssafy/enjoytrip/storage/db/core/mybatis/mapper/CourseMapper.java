package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.CourseItemDetailRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseItemRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecommendationCandidateRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseRecord;
import com.ssafy.enjoytrip.storage.db.core.model.CourseTagRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CourseMapper {
    int insert(CourseRecord record);

    CourseRecord findById(String id);

    List<CourseRecord> findByOwnerMemberId(Long ownerMemberId);

    int updateOwned(CourseRecord record);

    int updateStartLocation(@Param("id") String id,
                            @Param("longitude") Double longitude,
                            @Param("latitude") Double latitude);

    int softDeleteOwned(@Param("id") String id, @Param("ownerMemberId") Long ownerMemberId);

    int deleteItemsByCourseId(String courseId);

    int insertItem(CourseItemRecord record);

    int insertItems(List<CourseItemRecord> records);

    List<CourseItemRecord> findItemIdsByCourseId(String courseId);

    List<CourseItemDetailRecord> findItemsByCourseId(String courseId);

    List<CourseItemDetailRecord> findPublicItemsByCourseId(String courseId);

    List<CourseItemDetailRecord> findPublicItemsByCourseIds(
            @Param("courseIds") List<String> courseIds
    );

    List<CourseTagRecord> findTagsByCourseIds(
            @Param("courseIds") List<String> courseIds
    );

    List<CourseRecord> findDistanceOrderedPublicFeed(@Param("longitude") double longitude,
                                                      @Param("latitude") double latitude,
                                                      @Param("limit") int limit,
                                                      @Param("radiusMeters") Double radiusMeters);

    List<CourseRecord> findByRegionOrderedBySaveCount(@Param("regionName") String regionName,
                                                      @Param("limit") int limit);

    List<CourseRecord> findByRegionPrefixOrderedBySaveCount(@Param("prefix") String prefix,
                                                            @Param("limit") int limit);

    List<CourseRecord> findAllBySaveCount(@Param("limit") int limit);

    List<CourseRecommendationCandidateRecord> findCandidatesByMemberProfile(
            @Param("memberId") Long memberId,
            @Param("candidateLimit") int candidateLimit
    );

    boolean hasMemberProfileEmbedding(@Param("memberId") Long memberId);

    List<String> findRecentlyViewedCourseIds(
            @Param("memberId") Long memberId,
            @Param("days") int days
    );

    int insertView(@Param("courseId") String courseId, @Param("memberId") Long memberId);

    int insertSave(@Param("courseId") String courseId, @Param("memberId") Long memberId);

    int deleteSave(@Param("courseId") String courseId, @Param("memberId") Long memberId);

    List<CourseTagRecord> findTagsByCourseId(@Param("courseId") String courseId);

    int insertCourseTag(@Param("courseId") String courseId, @Param("tagId") Long tagId);

    int deleteTagsByCourseId(@Param("courseId") String courseId);

    int updateThumbnailUrl(@Param("courseId") String courseId);
}
