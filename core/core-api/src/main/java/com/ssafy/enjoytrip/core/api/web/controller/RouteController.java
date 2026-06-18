package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_POINTS;
import static com.ssafy.enjoytrip.core.support.error.ErrorType.INVALID_REQUEST;
import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.Point;
import com.ssafy.enjoytrip.core.domain.service.RouteOptimizationService;
import com.ssafy.enjoytrip.core.domain.service.RouteOptimizationService.SplitResult;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.RouteApi;
import com.ssafy.enjoytrip.core.api.web.dto.response.RouteOptimizeResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.RouteSplitByDayResponse;
import com.ssafy.enjoytrip.core.api.web.mapper.RoutePointQueryParser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController implements RouteApi {
    private final RouteOptimizationService service;

    @GetMapping("/optimizations")
    @Override
    public ApiResponse<RouteOptimizeResponse> optimize(@RequestParam(required = false) String points) {
        List<Point> parsed = RoutePointQueryParser.parse(points, INVALID_POINTS);
        int[] order = service.optimizeOrder(parsed);
        return success(new RouteOptimizeResponse(
                order,
                Double.parseDouble(service.formatDouble(service.estimateTotalDistanceKm(parsed, order)))
        ));
    }

    @GetMapping("/day-splits")
    @Override
    public ApiResponse<RouteSplitByDayResponse> splitByDay(@RequestParam(required = false) String points,
                                                    @RequestParam(required = false) String days) {
        List<Point> parsed = RoutePointQueryParser.parse(points, INVALID_REQUEST);
        SplitResult result = service.splitByLargestGap(parsed, parseInt(days, 1));
        List<Double> formattedDistances = result.dayDistances().stream()
                .map(value -> Double.parseDouble(service.formatDouble(value)))
                .toList();
        return success(new RouteSplitByDayResponse(result.days(), formattedDistances));
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

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
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
