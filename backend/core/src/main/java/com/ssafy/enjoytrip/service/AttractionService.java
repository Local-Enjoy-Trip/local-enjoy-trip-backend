package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.domain.AttractionSearchCondition;
import com.ssafy.enjoytrip.domain.AttractionStats;
import com.ssafy.enjoytrip.domain.AttractionTag;
import com.ssafy.enjoytrip.repository.AttractionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttractionService {
    private final AttractionRepository repository;

    public List<Attraction> searchAttractions(AttractionSearchCondition condition) {
        return repository.search(condition);
    }

    public List<Attraction> searchAttractions(AttractionSearchCondition condition, String userId) {
        return repository.search(condition, userId);
    }

    public boolean existsById(Long attractionId) {
        return repository.existsById(attractionId);
    }

    public AttractionStats findStats(Long attractionId, String userId) {
        return repository.findStats(attractionId, userId);
    }

    public void addFavorite(Long attractionId, String userId) {
        repository.addFavorite(attractionId, userId);
    }

    public boolean removeFavorite(Long attractionId, String userId) {
        return repository.removeFavorite(attractionId, userId);
    }

    public void upsertRating(Long attractionId, String userId, int rating) {
        repository.upsertRating(attractionId, userId, rating);
    }

    public boolean removeRating(Long attractionId, String userId) {
        return repository.removeRating(attractionId, userId);
    }

    public List<AttractionTag> findAllTags() {
        return repository.findAllTags();
    }

    public AttractionTag insertTag(String name) {
        return repository.insertTag(name);
    }

    public boolean updateTag(Long tagId, String name) {
        return repository.updateTag(tagId, name);
    }

    public boolean deleteTag(Long tagId) {
        return repository.deleteTag(tagId);
    }

    public boolean replaceTags(Long attractionId, List<Long> tagIds) {
        return repository.replaceTags(attractionId, tagIds);
    }
}
