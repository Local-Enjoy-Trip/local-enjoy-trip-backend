package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.POST_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.BoardPost;
import com.ssafy.enjoytrip.core.support.error.CoreException;
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
    public void updatePost(BoardPost post) {
        BoardPostRecord record = boardPostMapper.findById(post.id());
        if (record == null) {
            throw new CoreException(POST_NOT_FOUND);
        }
        record.update(post.title(), post.content());
        boardPostMapper.update(record);
    }

    @Transactional
    public void deletePost(String id) {
        if (boardPostMapper.existsById(id) <= 0) {
            throw new CoreException(POST_NOT_FOUND);
        }
        boardPostMapper.deleteById(id);
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
