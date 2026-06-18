package com.ssafy.enjoytrip.storage.db.core.jpa;

import com.ssafy.enjoytrip.storage.db.core.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {
    List<MemberEntity> findAllByOrderByCreatedAtDesc();

    Optional<MemberEntity> findByUserId(String userId);

    Optional<MemberEntity> findByEmail(String email);

    Optional<MemberEntity> findByUserIdAndEmail(String userId, String email);

    boolean existsByUserId(String userId);

    boolean existsByEmail(String email);

    void deleteByUserId(String userId);
}
