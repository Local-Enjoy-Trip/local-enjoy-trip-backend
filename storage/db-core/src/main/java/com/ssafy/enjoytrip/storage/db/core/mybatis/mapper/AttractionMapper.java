package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionAdminRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionPopularityDeltaRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionStatsRowRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AttractionMapper {
    List<AttractionSearchRecord> search(@Param("contentTypeId") String contentTypeId,
                                        @Param("keyword") String keyword,
                                        @Param("sidoCode") Integer sidoCode,
                                        @Param("gugunCode") Integer gugunCode,
                                        @Param("longitude") Double longitude,
                                        @Param("latitude") Double latitude,
                                        @Param("radiusMeters") Double radiusMeters,
                                        @Param("aroundSearch") boolean aroundSearch,
                                        @Param("limit") int limit,
                                        @Param("viewerMemberId") Long viewerMemberId);

    List<AttractionSearchRecord> findNearby(@Param("longitude") double longitude,
                                            @Param("latitude") double latitude,
                                            @Param("radiusMeters") double radiusMeters,
                                            @Param("limit") Integer limit,
                                            @Param("savedOnly") boolean savedOnly,
                                            @Param("viewerMemberId") Long viewerMemberId);

    List<AttractionSearchRecord> searchMapPlaces(@Param("keyword") String keyword,
                                                 @Param("escapedKeyword") String escapedKeyword,
                                                 @Param("longitude") double longitude,
                                                 @Param("latitude") double latitude,
                                                 @Param("radiusMeters") Double radiusMeters,
                                                 @Param("limit") Integer limit,
                                                 @Param("viewerMemberId") Long viewerMemberId);

    AttractionSearchRecord findDetailById(@Param("attractionId") Long attractionId,
                                         @Param("viewerMemberId") Long viewerMemberId);

    int existsById(Long attractionId);

    int existsPublicVisibleById(Long attractionId);

    List<AttractionAdminRecord> findAllForAdmin(@Param("includeHidden") boolean includeHidden);

    List<AttractionAdminRecord> findAdminPage(@Param("includeHidden") boolean includeHidden,
                                              @Param("limit") int limit,
                                              @Param("offset") int offset);

    int countForAdmin(@Param("includeHidden") boolean includeHidden);

    int insertAdmin(AttractionAdminRecord record);

    int updateAdmin(AttractionAdminRecord record);

    int hideForAdmin(@Param("attractionId") Long attractionId);

    int restoreForAdmin(@Param("attractionId") Long attractionId);

    List<AttractionSearchRecord> findSavedByMemberId(@Param("viewerMemberId") Long memberId);

    int insertSave(@Param("attractionId") Long attractionId, @Param("memberId") Long memberId);

    int deleteSave(@Param("attractionId") Long attractionId, @Param("memberId") Long memberId);

    int upsertRating(@Param("attractionId") Long attractionId,
                     @Param("memberId") Long memberId,
                     @Param("rating") int rating);

    int deleteRating(@Param("attractionId") Long attractionId, @Param("memberId") Long memberId);

    int refreshPopularityRatingStats(Long attractionId);

    List<AttractionStatsRowRecord> findStatsRowsByAttractionId(@Param("attractionId") Long attractionId,
                                                               @Param("memberId") Long memberId);

    List<AttractionStatsRowRecord> findStatsRowsByAttractionIds(@Param("ids") List<Long> ids,
                                                                @Param("memberId") Long memberId);

    List<AttractionCountRecord> findPopularityCounts(@Param("ids") List<Long> ids);

    int applyPopularitySaveDeltas(@Param("deltas") List<AttractionPopularityDeltaRecord> deltas);

    List<AttractionRecord> findByIds(@Param("ids") List<Long> ids);
}
