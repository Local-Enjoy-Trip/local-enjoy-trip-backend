package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.service.EvChargerService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.ChargerApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.ChargerSearchRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.ChargersResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chargers")
@RequiredArgsConstructor
@Validated
public class ChargerController implements ChargerApi {
    private final EvChargerService service;

    @GetMapping
    @Override
    public ApiResponse<ChargersResponse> find(@Valid @ModelAttribute ChargerSearchRequest request) {
        return success(new ChargersResponse(service.findChargers(
                request.normalizedZcode(),
                request.normalizedKeyword(),
                request.pageNoOrDefault(),
                request.numOfRowsOrDefault()
        )));
    }
}
