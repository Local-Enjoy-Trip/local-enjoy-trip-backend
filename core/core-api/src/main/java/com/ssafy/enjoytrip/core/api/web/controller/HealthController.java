package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.web.api.HealthApi;
import com.ssafy.enjoytrip.core.api.web.dto.response.DbHealthResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.HealthResponse;
import com.ssafy.enjoytrip.core.domain.service.DbHealthService;
import com.ssafy.enjoytrip.core.support.error.exception.InfraUnavailableException;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController implements HealthApi {
    private static final String DATABASE_DISCONNECTED_MESSAGE = "데이터베이스 연결이 끊어졌습니다.";

    private final DbHealthService dbHealthService;

    @GetMapping("/health")
    @Override
    public ApiResponse<HealthResponse> health() {
        return success(new HealthResponse("ok"));
    }

    @GetMapping("/api/db/health")
    @Override
    public ApiResponse<DbHealthResponse> dbHealth() {
        if (!dbHealthService.isConnected()) {
            throw new InfraUnavailableException(DATABASE_DISCONNECTED_MESSAGE);
        }
        return success(new DbHealthResponse("ok", "connected"));
    }
}
