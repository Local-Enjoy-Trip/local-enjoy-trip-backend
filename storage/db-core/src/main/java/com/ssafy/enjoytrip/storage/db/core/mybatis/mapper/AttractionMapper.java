package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.mybatis.row.AttractionAverageRatingRow;
import com.ssafy.enjoytrip.storage.db.core.mybatis.row.AttractionCountRow;
import com.ssafy.enjoytrip.storage.db.core.mybatis.row.AttractionRatingRow;
import com.ssafy.enjoytrip.storage.db.core.mybatis.row.AttractionRow;
import com.ssafy.enjoytrip.storage.db.core.mybatis.row.AttractionSearchRow;
import com.ssafy.enjoytrip.storage.db.core.mybatis.row.AttractionTagRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AttractionMapper {
    List<AttractionSearchRow> search(@Param("contentTypeId") String contentTypeId,
                                     @Param("keyword") String keyword,
                                     @Param("sidoCode") Integer sidoCode,
                                     @Param("gugunCode") Integer gugunCode,
                                     @Param("longitude") Double longitude,
                                     @Param("latitude") Double latitude,
                                     @Param("radiusMeters") Double radiusMeters,
                                     @Param("aroundSearch") boolean aroundSearch,
                                     @Param("limit") int limit);

    List<AttractionSearchRow> findNearby(@Param("longitude") double longitude,
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

    List<AttractionTagRow> findAllTags();

    AttractionTagRow insertTag(String name);

    int updateTag(@Param("tagId") Long tagId, @Param("name") String name);

    int deleteTag(Long tagId);

    int countTagsByIds(@Param("ids") List<Long> ids);

    int deleteTagMappings(Long attractionId);

    int insertTagMapping(@Param("attractionId") Long attractionId, @Param("tagId") Long tagId);

    List<AttractionCountRow> findFavoriteCounts(@Param("ids") List<Long> ids);

    List<AttractionAverageRatingRow> findRatingStats(@Param("ids") List<Long> ids);

    List<AttractionTagRow> findTagsByAttractionId(Long attractionId);

    List<AttractionRatingRow> findMyRatings(@Param("ids") List<Long> ids, @Param("userId") String userId);

    List<Long> findFavoritedIds(@Param("ids") List<Long> ids, @Param("userId") String userId);

    List<AttractionRow> findByIds(@Param("ids") List<Long> ids);
}
