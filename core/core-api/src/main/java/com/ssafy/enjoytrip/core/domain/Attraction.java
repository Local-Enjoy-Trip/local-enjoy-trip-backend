package com.ssafy.enjoytrip.core.domain;

import com.ssafy.enjoytrip.core.domain.vo.Address;
import com.ssafy.enjoytrip.core.domain.vo.Coordinate;
import com.ssafy.enjoytrip.core.domain.vo.RatingStats;

public record Attraction(
        Long id,
        String title,
        Address address,
        String tel,
        String firstImage,
        String firstImage2,
        Integer readcount,
        Integer sidoCode,
        Integer gugunCode,
        Coordinate location,
        String mlevel,
        String contentTypeId,
        String overview,
        int saveCount,
        RatingStats ratingStats,
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
