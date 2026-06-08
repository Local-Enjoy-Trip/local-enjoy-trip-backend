package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.Notice;

import java.util.List;

public interface NoticeRepository {
    List<Notice> findAll();

    void insert(Notice notice);

    boolean update(Notice notice);

    boolean delete(Long id);
}
