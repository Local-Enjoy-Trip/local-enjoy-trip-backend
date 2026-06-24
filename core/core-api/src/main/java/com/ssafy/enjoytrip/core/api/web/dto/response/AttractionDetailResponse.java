package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Attraction;

public record AttractionDetailResponse(
        Long id,
        String title,
        String address,
        String addressDetail,
        String zipcode,
        String tel,
        String imageUrl,
        Integer readcount,
        Double latitude,
        Double longitude,
        String contentTypeId,
        String overview,
        int saveCount,
        double ratingAverage,
        int ratingCount,
        boolean saved,
        Integer myRating
) {
    public AttractionDetailResponse(Attraction attraction) {
        this(
                attraction.id(),
                attraction.title(),
                attraction.address() != null ? attraction.address().address() : null,
                attraction.address() != null ? attraction.address().addressDetail() : null,
                attraction.address() != null ? attraction.address().zipcode() : null,
                attraction.tel(),
                attraction.primaryImageUrl(),
                attraction.readcount(),
                attraction.location() != null ? attraction.location().latitude() : null,
                attraction.location() != null ? attraction.location().longitude() : null,
                attraction.contentTypeId(),
                attraction.overview(),
                attraction.saveCount(),
                attraction.ratingStats() != null ? attraction.ratingStats().ratingAverage() : 0.0,
                attraction.ratingStats() != null ? attraction.ratingStats().ratingCount() : 0,
                attraction.saved(),
                attraction.myRating()
        );
    }
}
