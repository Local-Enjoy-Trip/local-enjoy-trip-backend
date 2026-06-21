package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.PopularAttraction;

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
        int favoriteCount,
        int saveCount,
        boolean saved,
        long popularityCount,
        double distanceMeters
) {
    public PopularAttractionResponse(PopularAttraction popularAttraction) {
        this(
                popularAttraction.attraction().id(),
                popularAttraction.attraction().title(),
                popularAttraction.attraction().addr1(),
                popularAttraction.attraction().addr2(),
                popularAttraction.attraction().firstImage(),
                popularAttraction.attraction().readcount(),
                popularAttraction.attraction().latitude(),
                popularAttraction.attraction().longitude(),
                popularAttraction.attraction().contentTypeId(),
                popularAttraction.attraction().favoriteCount(),
                popularAttraction.attraction().saveCount(),
                popularAttraction.attraction().saved(),
                popularAttraction.popularityCount(),
                popularAttraction.distanceMeters()
        );
    }

    public PopularAttractionResponse(Attraction attraction, long popularityCount, double distanceMeters) {
        this(new PopularAttraction(attraction, distanceMeters, popularityCount));
    }
}
