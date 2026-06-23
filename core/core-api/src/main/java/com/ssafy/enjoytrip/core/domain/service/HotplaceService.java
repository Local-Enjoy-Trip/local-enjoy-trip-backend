package com.ssafy.enjoytrip.core.domain.service;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.HOTPLACE_NOT_FOUND;

import com.ssafy.enjoytrip.core.domain.Hotplace;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.storage.db.core.model.HotplaceRecord;
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
                .map(HotplaceService::toHotplace)
                .toList();
    }

    public List<Hotplace> findHotplacesByMemberId(Long memberId) {
        return hotplaceMapper.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(HotplaceService::toHotplace)
                .toList();
    }

    public void insertHotplace(Hotplace hotplace) {
        hotplaceMapper.insert(new HotplaceRecord(
                hotplace.id(),
                hotplace.memberId(),
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
    public void deleteHotplaceOrThrow(String id, Long memberId) {
        if (hotplaceMapper.deleteByIdAndMemberId(id, memberId) <= 0) {
            throw new CoreException(HOTPLACE_NOT_FOUND);
        }
    }

    private static Hotplace toHotplace(HotplaceRecord record) {
        return new Hotplace(
                record.getId(),
                record.getMemberId(),
                record.getTitle(),
                record.getType(),
                record.getVisitDate(),
                record.getLat(),
                record.getLng(),
                record.getDescription(),
                record.getPhoto(),
                stringValue(record.getCreatedAt())
        );
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
