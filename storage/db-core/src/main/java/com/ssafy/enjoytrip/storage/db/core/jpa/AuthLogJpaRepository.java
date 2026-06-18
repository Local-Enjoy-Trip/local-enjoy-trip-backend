package com.ssafy.enjoytrip.storage.db.core.jpa;

import com.ssafy.enjoytrip.storage.db.core.entity.AuthLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthLogJpaRepository extends JpaRepository<AuthLogEntity, Long> {
}
