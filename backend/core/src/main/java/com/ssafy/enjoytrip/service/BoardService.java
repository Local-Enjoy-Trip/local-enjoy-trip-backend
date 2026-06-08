package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.BoardPost;
import com.ssafy.enjoytrip.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository repository;

    public List<BoardPost> findAllPosts() {
        return repository.findAll();
    }

    public void insertPost(BoardPost post) {
        repository.insert(post);
    }

    public boolean updatePost(BoardPost post) {
        return repository.update(post);
    }

    public boolean deletePost(String id) {
        return repository.delete(id);
    }
}
