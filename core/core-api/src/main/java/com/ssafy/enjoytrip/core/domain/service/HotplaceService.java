package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Hotplace;
import com.ssafy.enjoytrip.storage.db.core.entity.HotplaceEntity;
import com.ssafy.enjoytrip.storage.db.core.jpa.HotplaceJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotplaceService {

    private final HotplaceJpaRepository jpaRepository;

    public List<Hotplace> findAllHotplaces() {
        return jpaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(entity -> new Hotplace(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getTitle(),
                        entity.getType(),
                        entity.getVisitDate(),
                        entity.getLat(),
                        entity.getLng(),
                        entity.getDescription(),
                        entity.getPhoto(),
                        stringValue(entity.getCreatedAt())
                ))
                .toList();
    }

    public List<Hotplace> findHotplacesByUser(String userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(entity -> new Hotplace(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getTitle(),
                        entity.getType(),
                        entity.getVisitDate(),
                        entity.getLat(),
                        entity.getLng(),
                        entity.getDescription(),
                        entity.getPhoto(),
                        stringValue(entity.getCreatedAt())
                ))
                .toList();
    }

    public void insertHotplace(Hotplace hotplace) {
        jpaRepository.save(new HotplaceEntity(
                hotplace.id(),
                hotplace.userId(),
                hotplace.title(),
                hotplace.type(),
                hotplace.visitDate(),
                hotplace.lat(),
                hotplace.lng(),
                hotplace.description(),
                hotplace.photo()
        ));
    }

    @Transactional
    public boolean deleteHotplace(String id) {
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
