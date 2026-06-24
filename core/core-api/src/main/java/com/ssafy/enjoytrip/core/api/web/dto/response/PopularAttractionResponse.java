package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.PopularAttractionResult;

public record PopularAttractionResponse(
        Long id,
        String title,
        String addr1,
        String addr2,
        String firstImage,
        Integer readcount,
        Double latitude,
        Double longitude,
        String contentTypeId,
        int saveCount,
        boolean saved,
        long popularityCount,
        double distanceMeters
) {
    public PopularAttractionResponse(PopularAttractionResult popularAttraction) {
        this(
                popularAttraction.attraction().id(),
                popularAttraction.attraction().title(),
                addr1(popularAttraction.attraction()),
                addr2(popularAttraction.attraction()),
                popularAttraction.attraction().firstImage(),
                popularAttraction.attraction().readcount(),
                latitude(popularAttraction.attraction()),
                longitude(popularAttraction.attraction()),
                popularAttraction.attraction().contentTypeId(),
                popularAttraction.attraction().saveCount(),
                popularAttraction.attraction().saved(),
                popularAttraction.popularityCount(),
                popularAttraction.distanceMeters()
        );
    }

    public PopularAttractionResponse(Attraction attraction, long popularityCount, double distanceMeters) {
        this(new PopularAttractionResult(attraction, distanceMeters, popularityCount));
    }

    private static String addr1(Attraction a) {
        return a.address() != null ? a.address().address() : null;
    }

    private static String addr2(Attraction a) {
        return a.address() != null ? a.address().addressDetail() : null;
    }

    private static Double latitude(Attraction a) {
        return a.location() != null ? a.location().latitude() : null;
    }

    private static Double longitude(Attraction a) {
        return a.location() != null ? a.location().longitude() : null;
    }
}
