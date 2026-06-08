package com.ssafy.enjoytrip.storage.repository;

import com.ssafy.enjoytrip.domain.Notice;
import com.ssafy.enjoytrip.repository.NoticeRepository;
import com.ssafy.enjoytrip.storage.entity.NoticeEntity;
import com.ssafy.enjoytrip.storage.jpa.NoticeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NoticeStorageRepository implements NoticeRepository {
    private final NoticeJpaRepository jpaRepository;

    @Override
    public List<Notice> findAll() {
        return jpaRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toModel).toList();
    }

    @Override
    @Transactional
    public void insert(Notice notice) {
        jpaRepository.save(new NoticeEntity(notice.title(), notice.content(), notice.author()));
    }

    @Override
    @Transactional
    public boolean update(Notice notice) {
        return jpaRepository.findById(notice.id())
                .map(entity -> {
                    entity.update(notice.title(), notice.content());
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    private Notice toModel(NoticeEntity entity) {
        return new Notice(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getAuthor(),
                stringValue(entity.getCreatedAt()),
                stringValue(entity.getUpdatedAt())
        );
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
