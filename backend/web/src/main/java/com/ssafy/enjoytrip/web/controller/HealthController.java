package com.ssafy.enjoytrip.web.controller;

import com.ssafy.enjoytrip.web.api.*;

import static com.ssafy.enjoytrip.support.error.ErrorType.DATABASE_DISCONNECTED;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.repository.DbHealthRepository;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.response.DbHealthResponse;
import com.ssafy.enjoytrip.web.dto.response.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController implements HealthApi {
    private final DbHealthRepository dbHealthRepository;

    @GetMapping("/health")
    @Override
    public ApiResponse<HealthResponse> health() {
        return success(new HealthResponse("ok"));
    }

    @GetMapping("/api/db/health")
    @Override
    public ApiResponse<DbHealthResponse> dbHealth() {
        if (!dbHealthRepository.isConnected()) {
            throw new CoreException(DATABASE_DISCONNECTED);
        }
        return success(new DbHealthResponse("ok", "connected"));
    }

}
