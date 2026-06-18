package com.ssafy.enjoytrip.core.api.web.controller;

import com.ssafy.enjoytrip.core.domain.service.WeatherService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.WeatherApi;
import com.ssafy.enjoytrip.core.api.web.dto.response.WeatherBriefingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController implements WeatherApi {
    private final WeatherService service;

    @GetMapping("/briefings")
    @Override
    public ApiResponse<WeatherBriefingsResponse> findWeatherBriefings() {
        return success(new WeatherBriefingsResponse(service.findWeatherBriefings()));
    }
}
