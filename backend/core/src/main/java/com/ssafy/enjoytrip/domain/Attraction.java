package com.ssafy.enjoytrip.domain;

import java.util.List;

public record Attraction(
        Long id,
        String title,
        String addr1,
        String addr2,
        String zipcode,
        String tel,
        String firstImage,
        String firstImage2,
        Integer readcount,
        Integer sidoCode,
        Integer gugunCode,
        Double latitude,
        Double longitude,
        String mlevel,
        String contentTypeId,
        String overview,
        int favoriteCount,
        double ratingAverage,
        int ratingCount,
        List<AttractionTag> tags,
        boolean favorited,
        Integer myRating
) {
}
