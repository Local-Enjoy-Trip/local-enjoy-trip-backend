package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.domain.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.AttractionStats;
import com.ssafy.enjoytrip.domain.AttractionTag;

import java.util.List;

public interface AttractionRepository {
    List<Attraction> search(AttractionSearchCondition condition);

    default List<Attraction> search(AttractionSearchCondition condition, String userId) {
        return search(condition);
    }

    boolean existsById(Long attractionId);

    AttractionStats findStats(Long attractionId, String userId);

    void addFavorite(Long attractionId, String userId);

    boolean removeFavorite(Long attractionId, String userId);

    void upsertRating(Long attractionId, String userId, int rating);

    boolean removeRating(Long attractionId, String userId);

    List<AttractionTag> findAllTags();

    AttractionTag insertTag(String name);

    boolean updateTag(Long tagId, String name);

    boolean deleteTag(Long tagId);

    boolean replaceTags(Long attractionId, List<Long> tagIds);
}
