package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_POINTS;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_REQUEST;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Point;
import com.ssafy.enjoytrip.service.RouteOptimizationService;
import com.ssafy.enjoytrip.service.RouteOptimizationService.SplitResult;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.RouteApi;
import com.ssafy.enjoytrip.web.dto.response.RouteOptimizeResponse;
import com.ssafy.enjoytrip.web.dto.response.RouteSplitByDayResponse;
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
        if (hasInvalidPoints(points)) {
            throw new CoreException(INVALID_POINTS);
        }
        List<Point> parsed = service.parsePoints(points);
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
        if (hasInvalidPoints(points)) {
            throw new CoreException(INVALID_REQUEST);
        }
        SplitResult result = service.splitByLargestGap(service.parsePoints(points), parseInt(days, 1));
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

    private static boolean hasInvalidPoints(String raw) {
        String value = trim(raw);
        if (value.isEmpty()) {
            return false;
        }
        String[] chunks = value.split("\\|");
        for (String chunk : chunks) {
            String[] pair = chunk.split(",");
            if (pair.length != 2) {
                return true;
            }
            if (!isDouble(trim(pair[0])) || !isDouble(trim(pair[1]))) {
                return true;
            }
        }
        return false;
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

    private static boolean isDouble(String value) {
        if (value.isEmpty()) {
            return false;
        }
        if (value.equals("-") || value.equals(".") || value.equals("-.")) {
            return false;
        }
        boolean dotSeen = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (i == 0 && current == '-') {
                continue;
            }
            if (current == '.') {
                if (dotSeen) {
                    return false;
                }
                dotSeen = true;
                continue;
            }
            if (!Character.isDigit(current)) {
                return false;
            }
        }
        return true;
    }

}
