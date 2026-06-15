package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.AUTHENTICATION_REQUIRED;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.service.NoteImageUploadService;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.NoteImageApi;
import com.ssafy.enjoytrip.web.dto.request.NoteImagePresignedUploadRequest;
import com.ssafy.enjoytrip.web.dto.response.NoteImagePresignedUploadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/note-images")
@RequiredArgsConstructor
public class NoteImageController implements NoteImageApi {
    private final NoteImageUploadService service;

    @PostMapping("/presigned-upload")
    @Override
    public ApiResponse<NoteImagePresignedUploadResponse> createPresignedUpload(
            @Valid @RequestBody NoteImagePresignedUploadRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        NoteImageUploadUrl upload = service.createPresignedUpload(
                request.toCommand(authenticatedUserId(jwt))
        );

        return success(NoteImagePresignedUploadResponse.from(upload));
    }

    private static String authenticatedUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new CoreException(AUTHENTICATION_REQUIRED);
        }
        return jwt.getSubject().strip();
    }
}
