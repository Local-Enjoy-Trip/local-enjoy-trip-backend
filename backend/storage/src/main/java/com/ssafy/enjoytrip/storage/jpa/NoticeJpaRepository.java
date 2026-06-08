package com.ssafy.enjoytrip.storage.jpa;

import com.ssafy.enjoytrip.storage.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeJpaRepository extends JpaRepository<NoticeEntity, Long> {
    List<NoticeEntity> findAllByOrderByCreatedAtDesc();
}
