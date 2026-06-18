package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Notice;
import com.ssafy.enjoytrip.storage.db.core.entity.NoticeEntity;
import com.ssafy.enjoytrip.storage.db.core.jpa.NoticeJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeJpaRepository jpaRepository;

    public List<Notice> findAllNotices() {
        return jpaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(entity -> new Notice(
                        entity.getId(),
                        entity.getTitle(),
                        entity.getContent(),
                        entity.getAuthor(),
                        stringValue(entity.getCreatedAt()),
                        stringValue(entity.getUpdatedAt())
                ))
                .toList();
    }

    public void insertNotice(Notice notice) {
        jpaRepository.save(new NoticeEntity(notice.title(), notice.content(), notice.author()));
    }

    @Transactional
    public boolean updateNotice(Notice notice) {
        return jpaRepository.findById(notice.id())
                .map(entity -> {
                    entity.update(notice.title(), notice.content());
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean deleteNotice(Long id) {
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
