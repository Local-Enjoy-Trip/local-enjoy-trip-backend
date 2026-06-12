package com.ssafy.enjoytrip.web.dto.response;

import com.ssafy.enjoytrip.domain.Attraction;
import com.ssafy.enjoytrip.domain.PopularAttraction;

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
                popularAttraction.popularityCount(),
                popularAttraction.distanceMeters()
        );
    }

    public PopularAttractionResponse(Attraction attraction, long popularityCount, double distanceMeters) {
        this(new PopularAttraction(attraction, distanceMeters, popularityCount));
    }
}
