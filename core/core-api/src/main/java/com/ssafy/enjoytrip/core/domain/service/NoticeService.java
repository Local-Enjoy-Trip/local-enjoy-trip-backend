package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Notice;
import com.ssafy.enjoytrip.storage.db.core.entity.NoticeEntity;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.NoticeMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeMapper noticeMapper;

    public List<Notice> findAllNotices() {
        return noticeMapper.findAllOrderByCreatedAtDesc().stream()
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
        noticeMapper.insert(new NoticeEntity(notice.title(), notice.content(), notice.author()));
    }

    @Transactional
    public boolean updateNotice(Notice notice) {
        NoticeEntity entity = noticeMapper.findById(notice.id());
        if (entity == null) {
            return false;
        }
        entity.update(notice.title(), notice.content());
        return noticeMapper.update(entity) > 0;
    }

    @Transactional
    public boolean deleteNotice(Long id) {
        if (noticeMapper.existsById(id) <= 0) {
            return false;
        }
        return noticeMapper.deleteById(id) > 0;
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
