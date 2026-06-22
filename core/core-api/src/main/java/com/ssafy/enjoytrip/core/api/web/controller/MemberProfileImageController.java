package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedUserId;
import com.ssafy.enjoytrip.core.api.web.api.MemberProfileImageApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.ProfileImagePresignedUploadRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.ProfileImageUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.ProfileImagePresignedUploadResponse;
import com.ssafy.enjoytrip.core.domain.ProfileImageUploadUrl;
import com.ssafy.enjoytrip.core.domain.service.MemberProfileImageService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/me/profile-image")
@RequiredArgsConstructor
public class MemberProfileImageController implements MemberProfileImageApi {
    private final MemberProfileImageService service;

    @PostMapping("/presigned-upload")
    @Override
    public ApiResponse<ProfileImagePresignedUploadResponse> createPresignedUpload(
            @Valid @RequestBody ProfileImagePresignedUploadRequest request,
            @AuthenticatedUserId String authenticatedUserId
    ) {
        ProfileImageUploadUrl upload = service.createPresignedUpload(
                authenticatedUserId,
                request.normalizedContentType(),
                request.normalizedFileExtension()
        );

        return success(ProfileImagePresignedUploadResponse.from(upload));
    }

    @PutMapping
    @Override
    public ApiResponse<Void> updateProfileImage(
            @Valid @RequestBody ProfileImageUpdateRequest request,
            @AuthenticatedUserId String authenticatedUserId
    ) {
        service.updateProfileImage(
                authenticatedUserId,
                request.normalizedObjectKeyFor(authenticatedUserId)
        );

        return success();
    }
}
