package com.ssafy.enjoytrip.web.controller;

import com.ssafy.enjoytrip.web.api.*;

import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.ChargerItem;
import com.ssafy.enjoytrip.service.EvChargerService;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.ChargerSearchRequest;
import com.ssafy.enjoytrip.web.dto.response.ChargersResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chargers")
@RequiredArgsConstructor
public class ChargerController implements ChargerApi {
    private final EvChargerService service;

    @GetMapping
    @Override
    public ApiResponse<ChargersResponse> find(@ModelAttribute ChargerSearchRequest request) {
        return success(new ChargersResponse(service.findChargers(
                trim(request.zcode()),
                trim(request.keyword()),
                parseInt(request.pageNo(), 1),
                parseInt(request.numOfRows(), 150)
        )));
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static int parseInt(String raw, int fallback) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return fallback;
        }
        if (!isInteger(value)) {
            return fallback;
        }
        return Integer.parseInt(value);
    }

    private static boolean isInteger(String value) {
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (i == 0 && current == '-') {
                continue;
            }
            if (!Character.isDigit(current)) {
                return false;
            }
        }
        return !value.equals("-");
    }

}
