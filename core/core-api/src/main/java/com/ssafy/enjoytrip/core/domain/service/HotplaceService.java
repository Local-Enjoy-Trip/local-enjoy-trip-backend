package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.Hotplace;
import com.ssafy.enjoytrip.storage.db.core.entity.HotplaceEntity;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.HotplaceMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotplaceService {
    private final HotplaceMapper hotplaceMapper;

    public List<Hotplace> findAllHotplaces() {
        return hotplaceMapper.findAllOrderByCreatedAtDesc().stream()
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
        return hotplaceMapper.findByUserIdOrderByCreatedAtDesc(userId).stream()
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
        hotplaceMapper.insert(new HotplaceEntity(
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
        if (hotplaceMapper.existsById(id) <= 0) {
            return false;
        }
        return hotplaceMapper.deleteById(id) > 0;
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
