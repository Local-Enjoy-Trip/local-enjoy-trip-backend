package com.ssafy.enjoytrip.storage.repository;

import com.ssafy.enjoytrip.domain.Hotplace;
import com.ssafy.enjoytrip.repository.HotplaceRepository;
import com.ssafy.enjoytrip.storage.entity.HotplaceEntity;
import com.ssafy.enjoytrip.storage.jpa.HotplaceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HotplaceStorageRepository implements HotplaceRepository {
    private final HotplaceJpaRepository jpaRepository;

    @Override
    public List<Hotplace> findAll() {
        return jpaRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toModel).toList();
    }

    @Override
    public List<Hotplace> findByUser(String userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toModel).toList();
    }

    @Override
    @Transactional
    public void insert(Hotplace hotplace) {
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

    @Override
    @Transactional
    public boolean delete(String id) {
        if (!jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    private Hotplace toModel(HotplaceEntity entity) {
        return new Hotplace(
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
        );
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
