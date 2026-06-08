package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.service.EvChargerService;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.ChargerApi;
import com.ssafy.enjoytrip.web.dto.request.ChargerSearchRequest;
import com.ssafy.enjoytrip.web.dto.response.ChargersResponse;
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
    public ApiResponse<ChargersResponse> find(@Validated @ModelAttribute ChargerSearchRequest request) {
        return success(new ChargersResponse(service.findChargers(
                trim(request.zcode()),
                trim(request.keyword()),
                request.pageNoOrDefault(),
                request.numOfRowsOrDefault()
        )));
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
