package com.ssafy.enjoytrip.web.controller;

import com.ssafy.enjoytrip.web.api.*;

import static com.ssafy.enjoytrip.support.error.ErrorType.HOTPLACE_NOT_FOUND;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_ACTION;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_LATITUDE_OR_LONGITUDE;
import static com.ssafy.enjoytrip.support.error.ErrorType.MISSING_ID;
import static com.ssafy.enjoytrip.support.error.ErrorType.MISSING_REQUIRED_FIELDS;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Hotplace;
import com.ssafy.enjoytrip.service.HotplaceService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorType;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.HotplaceRequest;
import com.ssafy.enjoytrip.web.dto.response.HotplacesResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotplaces")
@RequiredArgsConstructor
public class HotplaceController implements HotplaceApi {
    private final HotplaceService service;

    @GetMapping
    @Override
    public ApiResponse<HotplacesResponse> find(@RequestParam(required = false) String userId) {
        String trimmedUserId = trim(userId);
        if (trimmedUserId.isEmpty()) {
            return success(new HotplacesResponse(service.findAllHotplaces()));
        }
        List<Hotplace> hotplaces = service.findHotplacesByUser(trimmedUserId);
        return success(new HotplacesResponse(hotplaces));
    }

    @PostMapping
    @Override
    public ApiResponse<Void> legacyPost(@ModelAttribute HotplaceRequest request) {
        return switch (trim(request.action())) {
            case "create" -> create(request);
            case "delete" -> delete(trim(request.id()));
            default -> fail(INVALID_ACTION);
        };
    }

    @PostMapping("/items")
    @Override
    public ApiResponse<Void> create(@ModelAttribute HotplaceRequest request) {
        String id = trim(request.id());
        String userId = trim(request.userId());
        String title = trim(request.title());
        String type = trim(request.type());
        String visitDate = trim(request.visitDate());
        if (id.isEmpty() || userId.isEmpty() || title.isEmpty() || type.isEmpty() || visitDate.isEmpty()) {
            return fail(MISSING_REQUIRED_FIELDS);
        }

        String lat = trim(request.lat());
        String lng = trim(request.lng());
        if (!isDouble(lat) || !isDouble(lng)) {
            return fail(INVALID_LATITUDE_OR_LONGITUDE);
        }

        service.insertHotplace(new Hotplace(id, userId, title, type, visitDate,
                Double.parseDouble(lat),
                Double.parseDouble(lng),
                trim(request.description()), trim(request.photo()), ""));
        return success();
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> delete(@PathVariable String id) {
        if (trim(id).isEmpty()) {
            return fail(MISSING_ID);
        }
        if (service.deleteHotplace(id)) {
            return success();
        }
        return fail(HOTPLACE_NOT_FOUND);
    }

    private static <T> T fail(ErrorType error) {
        throw new CoreException(error);
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
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
