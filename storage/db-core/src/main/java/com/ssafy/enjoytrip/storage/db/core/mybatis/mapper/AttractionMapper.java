package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.AttractionAverageRatingRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionCountRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRatingRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionSearchRecord;
import com.ssafy.enjoytrip.storage.db.core.model.AttractionTagRecord;
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
                                     @Param("limit") int limit);

    List<AttractionSearchRecord> findNearby(@Param("longitude") double longitude,
                                         @Param("latitude") double latitude,
                                         @Param("radiusMeters") double radiusMeters,
                                         @Param("limit") int limit);

    int existsById(Long attractionId);

    int insertFavorite(@Param("attractionId") Long attractionId, @Param("userId") String userId);

    int deleteFavorite(@Param("attractionId") Long attractionId, @Param("userId") String userId);

    int upsertRating(@Param("attractionId") Long attractionId,
                     @Param("userId") String userId,
                     @Param("rating") int rating);

    int deleteRating(@Param("attractionId") Long attractionId, @Param("userId") String userId);

    List<AttractionTagRecord> findAllTags();

    AttractionTagRecord insertTag(String name);

    int updateTag(@Param("tagId") Long tagId, @Param("name") String name);

    int deleteTag(Long tagId);

    int countTagsByIds(@Param("ids") List<Long> ids);

    int deleteTagMappings(Long attractionId);

    int insertTagMapping(@Param("attractionId") Long attractionId, @Param("tagId") Long tagId);

    List<AttractionCountRecord> findFavoriteCounts(@Param("ids") List<Long> ids);

    List<AttractionCountRecord> findPopularityFavoriteCounts(@Param("ids") List<Long> ids);

    int updatePopularityFavoriteDelta(@Param("attractionId") Long attractionId,
                                      @Param("delta") Long delta);

    int insertPopularityFavoriteDeltaIfAbsent(@Param("attractionId") Long attractionId,
                                              @Param("delta") Long delta);

    List<AttractionAverageRatingRecord> findRatingStats(@Param("ids") List<Long> ids);

    List<AttractionTagRecord> findTagsByAttractionId(Long attractionId);

    List<AttractionRatingRecord> findMyRatings(@Param("ids") List<Long> ids, @Param("userId") String userId);

    List<Long> findFavoritedIds(@Param("ids") List<Long> ids, @Param("userId") String userId);

    List<AttractionRecord> findByIds(@Param("ids") List<Long> ids);
}
