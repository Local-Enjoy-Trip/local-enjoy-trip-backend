package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.TagRequest;
import com.ssafy.enjoytrip.web.dto.response.AttractionTagsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Attraction Tags", description = "관광지 태그 관리 API")
public interface AttractionTagApi {
    @Operation(summary = "태그 목록 조회", description = "관광지 태그 목록을 조회합니다.", operationId = "findAttractionTags")
    ApiResponse<AttractionTagsResponse> tags();

    @Operation(summary = "태그 생성", description = "관광지 태그를 생성합니다.", operationId = "createAttractionTag")
    ResponseEntity<ApiResponse<AttractionTagsResponse>> create(TagRequest request);

    @Operation(summary = "태그 수정", description = "관광지 태그 이름을 수정합니다.", operationId = "updateAttractionTag")
    ApiResponse<Void> update(Long id, TagRequest request);

    @Operation(summary = "태그 삭제", description = "관광지 태그를 삭제합니다.", operationId = "deleteAttractionTag")
    ApiResponse<Void> delete(Long id);
}
