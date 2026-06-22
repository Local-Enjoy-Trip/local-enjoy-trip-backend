package com.ssafy.enjoytrip.core.domain;

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
        int saveCount,
        double ratingAverage,
        int ratingCount,
        List<AttractionTag> tags,
        boolean saved,
        Integer myRating
) {
    public String primaryImageUrl() {
        if (firstImage != null && !firstImage.isBlank()) {
            return firstImage;
        }
        if (firstImage2 != null && !firstImage2.isBlank()) {
            return firstImage2;
        }
        return null;
    }
}
