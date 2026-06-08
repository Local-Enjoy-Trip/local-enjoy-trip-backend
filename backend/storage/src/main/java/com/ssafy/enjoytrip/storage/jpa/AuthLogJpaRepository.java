package com.ssafy.enjoytrip.storage.jpa;

import com.ssafy.enjoytrip.storage.entity.AuthLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthLogJpaRepository extends JpaRepository<AuthLogEntity, Long> {
}
