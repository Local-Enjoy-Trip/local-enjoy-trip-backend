package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefing;
import com.ssafy.enjoytrip.core.domain.service.NeighborhoodBriefingService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.NeighborhoodBriefingApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.NeighborhoodBriefingRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.NeighborhoodBriefingResponse;
import jakarta.validation.Valid;
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

    @GetMapping("/briefing")
    @Override
    public ApiResponse<NeighborhoodBriefingResponse> brief(
            @Valid @ModelAttribute NeighborhoodBriefingRequest request
    ) {
        NeighborhoodBriefing briefing = service.brief(request.toRegionName());

        return success(new NeighborhoodBriefingResponse(briefing));
    }
}
