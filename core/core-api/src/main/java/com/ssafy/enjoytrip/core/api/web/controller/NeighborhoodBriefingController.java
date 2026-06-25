package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.core.domain.WeatherWithForecast;
import com.ssafy.enjoytrip.core.domain.service.NeighborhoodBriefingService;
import com.ssafy.enjoytrip.core.domain.service.WeatherService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.NeighborhoodBriefingApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.NeighborhoodBriefingRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.NeighborhoodBriefingResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/neighborhood")
@RequiredArgsConstructor
public class NeighborhoodBriefingController implements NeighborhoodBriefingApi {
    private final NeighborhoodBriefingService service;
    private final WeatherService weatherService;

    @GetMapping("/briefing")
    @Override
    public ApiResponse<NeighborhoodBriefingResponse> brief(
            @ModelAttribute NeighborhoodBriefingRequest request
    ) {
        String currentHour = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

        WeatherWithForecast weatherWithForecast = weatherService.findWeatherWithForecast(
                request.latitude(),
                request.longitude(),
                request.toRegionName(),
                currentHour
        );

        NeighborhoodBriefing briefing = service.brief(
                request.toRegionName(),
                weatherWithForecast,
                currentHour
        );

        return success(new NeighborhoodBriefingResponse(briefing));
    }
}
