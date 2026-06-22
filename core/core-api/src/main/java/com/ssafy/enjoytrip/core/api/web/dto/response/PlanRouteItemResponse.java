package com.ssafy.enjoytrip.core.api.web.dto.response;

import com.ssafy.enjoytrip.core.domain.Attraction;
import com.ssafy.enjoytrip.core.domain.AttractionTag;
import com.ssafy.enjoytrip.core.domain.PlanRouteItem;

import java.util.List;

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
        int ratingCount,
        List<AttractionTag> tags
) {
    public static PlanRouteItemResponse from(PlanRouteItem item) {
        Attraction attraction = item.attraction();
        return new PlanRouteItemResponse(
                item.routeId(),
                item.routeItemId(),
                attraction.id(),
                item.attractionId(),
                attraction.title(),
                attraction.addr1(),
                attraction.addr2(),
                attraction.tel(),
                attraction.firstImage(),
                attraction.firstImage2(),
                attraction.readcount(),
                attraction.sidoCode(),
                attraction.gugunCode(),
                attraction.contentTypeId(),
                attraction.latitude(),
                attraction.longitude(),
                attraction.overview(),
                item.position(),
                item.day(),
                item.memo(),
                item.stayMinutes(),
                attraction.saveCount(),
                attraction.ratingAverage(),
                attraction.ratingCount(),
                attraction.tags()
        );
    }
}
