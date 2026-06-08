package com.ssafy.enjoytrip.storage.jpa;

import com.ssafy.enjoytrip.storage.entity.BoardPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardPostJpaRepository extends JpaRepository<BoardPostEntity, String> {
    List<BoardPostEntity> findAllByOrderByCreatedAtDesc();
}
