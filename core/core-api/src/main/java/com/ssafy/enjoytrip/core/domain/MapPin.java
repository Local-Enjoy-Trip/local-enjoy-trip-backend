package com.ssafy.enjoytrip.core.domain;

public interface MapPin {
    String type(); // 클라이언트 분기용 ("PLACE" 또는 "NOTE")
    int matchTier(); // 정렬용
    double distanceMeters(); // 정렬용
}
