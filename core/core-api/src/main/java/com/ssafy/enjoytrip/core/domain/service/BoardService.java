package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.BoardPost;
import com.ssafy.enjoytrip.storage.db.core.entity.BoardPostEntity;
import com.ssafy.enjoytrip.storage.db.core.jpa.BoardPostJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardPostJpaRepository jpaRepository;

    public List<BoardPost> findAllPosts() {
        return jpaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(entity -> new BoardPost(
                        entity.getId(),
                        entity.getTitle(),
                        entity.getContent(),
                        entity.getAuthor(),
                        stringValue(entity.getCreatedAt()),
                        stringValue(entity.getUpdatedAt())
                ))
                .toList();
    }

    public void insertPost(BoardPost post) {
        jpaRepository.save(new BoardPostEntity(post.id(), post.title(), post.content(), post.author()));
    }

    @Transactional
    public boolean updatePost(BoardPost post) {
        return jpaRepository.findById(post.id())
                .map(entity -> {
                    entity.update(post.title(), post.content());
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean deletePost(String id) {
        if (!jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
