package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.BoardPost;
import com.ssafy.enjoytrip.storage.db.core.entity.BoardPostEntity;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.BoardPostMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardPostMapper boardPostMapper;

    public List<BoardPost> findAllPosts() {
        return boardPostMapper.findAllOrderByCreatedAtDesc().stream()
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
        boardPostMapper.insert(new BoardPostEntity(post.id(), post.title(), post.content(), post.author()));
    }

    @Transactional
    public boolean updatePost(BoardPost post) {
        BoardPostEntity entity = boardPostMapper.findById(post.id());
        if (entity == null) {
            return false;
        }
        entity.update(post.title(), post.content());
        return boardPostMapper.update(entity) > 0;
    }

    @Transactional
    public boolean deletePost(String id) {
        if (boardPostMapper.existsById(id) <= 0) {
            return false;
        }
        return boardPostMapper.deleteById(id) > 0;
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
