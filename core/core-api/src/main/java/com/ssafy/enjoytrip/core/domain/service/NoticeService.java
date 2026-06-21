package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.NOTICE_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Notice;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.NoticeRecord;
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
                .map(record -> new Notice(
                        record.getId(),
                        record.getTitle(),
                        record.getContent(),
                        record.getAuthor(),
                        stringValue(record.getCreatedAt()),
                        stringValue(record.getUpdatedAt())
                ))
                .toList();
    }

    public void insertNotice(Notice notice) {
        noticeMapper.insert(new NoticeRecord(notice.title(), notice.content(), notice.author()));
    }

    @Transactional
    public void updateNoticeOrThrow(Notice notice) {
        if (!updateNotice(notice)) {
            throw new CoreException(NOTICE_NOT_FOUND);
        }
    }

    @Transactional
    public boolean updateNotice(Notice notice) {
        NoticeRecord record = noticeMapper.findById(notice.id());
        if (record == null) {
            return false;
        }
        record.update(notice.title(), notice.content());
        return noticeMapper.update(record) > 0;
    }

    @Transactional
    public void deleteNoticeOrThrow(Long id) {
        if (!deleteNotice(id)) {
            throw new CoreException(NOTICE_NOT_FOUND);
        }
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
