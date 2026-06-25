package com.ssafy.enjoytrip.storage.db.core.model;

public record LocalPlaceRecord(
        Long id,
        String title,
        String addr1,
        String addr2,
        String contentTypeId
) {
}
