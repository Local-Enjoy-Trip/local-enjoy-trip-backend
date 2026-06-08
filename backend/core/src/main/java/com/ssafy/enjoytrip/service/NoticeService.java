package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.domain.Notice;
import com.ssafy.enjoytrip.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository repository;

    public List<Notice> findAllNotices() {
        return repository.findAll();
    }

    public void insertNotice(Notice notice) {
        repository.insert(notice);
    }

    public boolean updateNotice(Notice notice) {
        return repository.update(notice);
    }

    public boolean deleteNotice(Long id) {
        return repository.delete(id);
    }
}
