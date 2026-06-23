package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import com.ssafy.enjoytrip.core.api.web.api.NoteImageApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.NoteImagePresignedUploadRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.NoteImagePresignedUploadResponse;
import com.ssafy.enjoytrip.core.domain.NoteImageUploadUrl;
import com.ssafy.enjoytrip.core.domain.service.NoteImageUploadService;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
            @AuthenticatedMemberId Long memberId
    ) {
        NoteImageUploadUrl upload = service.createPresignedUpload(
                memberId,
                request.normalizedContentType(),
                request.normalizedFileExtension()
        );

        return success(NoteImagePresignedUploadResponse.from(upload));
    }
}
