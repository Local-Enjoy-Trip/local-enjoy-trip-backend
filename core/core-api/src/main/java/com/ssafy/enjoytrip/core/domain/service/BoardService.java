package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.BoardPost;
import com.ssafy.enjoytrip.storage.db.core.model.BoardPostRecord;
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
                .map(record -> new BoardPost(
                        record.getId(),
                        record.getTitle(),
                        record.getContent(),
                        record.getAuthor(),
                        stringValue(record.getCreatedAt()),
                        stringValue(record.getUpdatedAt())
                ))
                .toList();
    }

    public void insertPost(BoardPost post) {
        boardPostMapper.insert(new BoardPostRecord(post.id(), post.title(), post.content(), post.author()));
    }

    @Transactional
    public boolean updatePost(BoardPost post) {
        BoardPostRecord record = boardPostMapper.findById(post.id());
        if (record == null) {
            return false;
        }
        record.update(post.title(), post.content());
        return boardPostMapper.update(record) > 0;
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
