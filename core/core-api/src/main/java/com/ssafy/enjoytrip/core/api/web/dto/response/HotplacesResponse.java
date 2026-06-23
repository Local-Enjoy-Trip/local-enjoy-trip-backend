package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Hotplace;
import java.util.List;
import java.util.Collection;

public record HotplacesResponse(List<HotplaceResponse> hotplaces) {
    public HotplacesResponse(Collection<Hotplace> hotplaces) {
        this(hotplaces.stream()
                .map(HotplaceResponse::from)
                .toList());
    }

    public record HotplaceResponse(
            String id,
            String title,
            String type,
            String visitDate,
            Double lat,
            Double lng,
            String description,
            String photo,
            String createdAt
    ) {
        static HotplaceResponse from(Hotplace hotplace) {
            return new HotplaceResponse(
                    hotplace.id(),
                    hotplace.title(),
                    hotplace.type(),
                    hotplace.visitDate(),
                    hotplace.lat(),
                    hotplace.lng(),
                    hotplace.description(),
                    hotplace.photo(),
                    hotplace.createdAt()
            );
        }
    }
}
