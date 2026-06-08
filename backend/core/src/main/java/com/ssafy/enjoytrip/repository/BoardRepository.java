package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.BoardPost;

import java.util.List;

public interface BoardRepository {
    List<BoardPost> findAll();

    void insert(BoardPost post);

    boolean update(BoardPost post);

    boolean delete(String id);
}
