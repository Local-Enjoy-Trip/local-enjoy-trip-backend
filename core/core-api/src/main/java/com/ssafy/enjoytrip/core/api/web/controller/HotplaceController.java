package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import com.ssafy.enjoytrip.core.api.web.api.HotplaceApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.HotplaceCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.HotplacesResponse;
import com.ssafy.enjoytrip.core.domain.Hotplace;
import com.ssafy.enjoytrip.core.domain.service.HotplaceService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotplaces")
@RequiredArgsConstructor
public class HotplaceController implements HotplaceApi {
    private final HotplaceService service;

    @GetMapping
    @Override
    public ApiResponse<HotplacesResponse> find(@RequestParam(required = false) Long memberId) {
        if (memberId == null) {
            return success(new HotplacesResponse(service.findAllHotplaces()));
        }
        List<Hotplace> hotplaces = service.findHotplacesByMemberId(memberId);
        return success(new HotplacesResponse(hotplaces));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<Void> create(
            @Valid @RequestBody HotplaceCreateRequest request,
            @AuthenticatedMemberId Long memberId
    ) {
        service.insertHotplace(new Hotplace(
                request.normalizedId(),
                memberId,
                request.normalizedTitle(),
                request.normalizedType(),
                request.normalizedVisitDate(),
                request.lat(),
                request.lng(),
                request.normalizedDescription(),
                request.normalizedPhoto(),
                ""
        ));

        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable String id, @AuthenticatedMemberId Long memberId) {
        service.deleteHotplaceOrThrow(id.strip(), memberId);
        return success();
    }
}
