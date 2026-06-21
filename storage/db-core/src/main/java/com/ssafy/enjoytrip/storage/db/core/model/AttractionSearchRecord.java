package com.ssafy.enjoytrip.storage.db.core.model;

public record AttractionSearchRecord(
        Long id,
        String title,
        String addr1,
        String addr2,
        String zipcode,
        String tel,
        String firstImage,
        String firstImage2,
        Integer readCount,
        Integer sidoCode,
        Integer gugunCode,
        Double latitude,
        Double longitude,
        String mlevel,
        String contentTypeId,
        String overview,
        Double distanceMeters,
        int favoriteCount,
        int saveCount,
        double ratingAverage,
        int ratingCount,
        Long tagId,
        String tagName,
        boolean favorited,
        boolean saved,
        Integer myRating
) {
}
