package com.ssafy.enjoytrip.storage.repository;

import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.repository.BoardRepository;
import com.ssafy.enjoytrip.storage.entity.BoardPostEntity;
import com.ssafy.enjoytrip.storage.jpa.BoardPostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BoardStorageRepository implements BoardRepository {
    private final BoardPostJpaRepository jpaRepository;

    @Override
    public List<BoardPost> findAll() {
        return jpaRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional
    public void insert(BoardPost post) {
        jpaRepository.save(new BoardPostEntity(post.id(), post.title(), post.content(), post.author()));
    }

    @Override
    @Transactional
    public boolean update(BoardPost post) {
        return jpaRepository.findById(post.id())
                .map(entity -> {
                    entity.update(post.title(), post.content());
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean delete(String id) {
        if (!jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    private BoardPost toModel(BoardPostEntity entity) {
        return new BoardPost(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getAuthor(),
                stringValue(entity.getCreatedAt()),
                stringValue(entity.getUpdatedAt())
        );
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
