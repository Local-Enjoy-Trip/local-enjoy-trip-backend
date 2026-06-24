package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.PlanRouteItem;

public record PlanRouteItemResponse(
        String routeId,
        Long routeItemId,
        Long id,
        Long attractionId,
        String title,
        String addr1,
        String addr2,
        String tel,
        String firstImage,
        String firstImage2,
        Integer readcount,
        Integer sidoCode,
        Integer gugunCode,
        String contentTypeId,
        Double latitude,
        Double longitude,
        String overview,
        int position,
        int day,
        String memo,
        int stayMinutes,
        int saveCount,
        double ratingAverage,
        int ratingCount
) {
    public static PlanRouteItemResponse from(PlanRouteItem item) {
        Attraction attraction = item.attraction();
        return new PlanRouteItemResponse(
                item.routeId(),
                item.routeItemId(),
                attraction.id(),
                item.attractionId(),
                attraction.title(),
                attraction.address() != null ? attraction.address().address() : null,
                attraction.address() != null ? attraction.address().addressDetail() : null,
                attraction.tel(),
                attraction.firstImage(),
                attraction.firstImage2(),
                attraction.readcount(),
                attraction.sidoCode(),
                attraction.gugunCode(),
                attraction.contentTypeId(),
                attraction.location() != null ? attraction.location().latitude() : null,
                attraction.location() != null ? attraction.location().longitude() : null,
                attraction.overview(),
                item.position(),
                item.day(),
                item.memo(),
                item.stayMinutes(),
                attraction.saveCount(),
                attraction.ratingStats() != null ? attraction.ratingStats().ratingAverage() : 0.0,
                attraction.ratingStats() != null ? attraction.ratingStats().ratingCount() : 0
        );
    }
}
