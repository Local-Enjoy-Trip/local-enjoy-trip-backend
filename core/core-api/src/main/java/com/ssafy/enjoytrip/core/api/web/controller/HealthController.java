package com.ssafy.enjoytrip.core.api.web.controller;

import com.ssafy.enjoytrip.core.api.web.api.*;

import static com.ssafy.enjoytrip.core.support.error.ErrorType.DATABASE_DISCONNECTED;
import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.service.DbHealthService;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.DbHealthResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController implements HealthApi {
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
            throw new CoreException(DATABASE_DISCONNECTED);
        }
        return success(new DbHealthResponse("ok", "connected"));
    }

}
