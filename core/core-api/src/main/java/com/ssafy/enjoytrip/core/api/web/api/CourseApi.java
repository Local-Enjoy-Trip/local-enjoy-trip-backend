package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.api.web.dto.request.CourseCreateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseFeedRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseOrderRecommendationRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CoursePopularFeedRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseRecommendationRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.CourseUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.CourseFeedResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.CourseResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.CoursesResponse;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;

@Tag(name = "Courses", description = "코스 피드, 추천, 생성, 수정, 삭제 API")
public interface CourseApi {

    @Operation(
            summary = "코스 피드 조회",
            description = "현재 위치 기준 공개 코스를 거리순으로 조회합니다.",
            operationId = "courseFeed"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "코스 피드 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseFeedResponse.class),
                            examples = @ExampleObject(value = ApiExamples.COURSE_FEED_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 좌표 또는 파라미터")
    })
    ApiResponse<CourseFeedResponse> feed(CourseFeedRequest request);

    @Operation(
            summary = "코스 추천 조회",
            description = "찜 기반 유사 코스를 추천합니다. 인증된 경우 개인화 추천, 미인증 시 동네 기반 폴백.",
            operationId = "courseRecommendations",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "코스 추천 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseFeedResponse.class),
                            examples = @ExampleObject(value = ApiExamples.COURSE_FEED_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터")
    })
    ApiResponse<CourseFeedResponse> recommendations(
            CourseRecommendationRequest request,
            @Parameter(hidden = true) Long authenticatedMemberId
    );

    @Operation(
            summary = "인기 코스 피드 조회",
            description = "특정 동네의 저장 수 기준 인기 코스를 조회합니다.",
            operationId = "coursePopularFeed"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "인기 코스 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseFeedResponse.class),
                            examples = @ExampleObject(value = ApiExamples.COURSE_FEED_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터")
    })
    ApiResponse<CourseFeedResponse> popularFeed(CoursePopularFeedRequest request);

    @Operation(
            summary = "코스 상세 조회",
            description = "코스 ID로 공개 코스 상세 정보를 조회합니다.",
            operationId = "courseDetail"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "코스 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class),
                            examples = @ExampleObject(value = ApiExamples.COURSE_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스 없음")
    })
    ApiResponse<CourseResponse> detail(
            @Parameter(description = "코스 ID", example = "c1") @NotBlank String id,
            @Parameter(hidden = true) Long authenticatedMemberId
    );

    @Operation(
            summary = "내 코스 목록 조회",
            description = "JWT subject 회원이 생성한 코스 목록을 조회합니다.",
            operationId = "myCourses",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 코스 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CoursesResponse.class),
                            examples = @ExampleObject(value = ApiExamples.COURSES_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ApiResponse<CoursesResponse> mine(@Parameter(hidden = true) Long authenticatedMemberId);

    @Operation(
            summary = "코스 생성",
            description = "새 코스를 생성합니다. `id`는 클라이언트에서 지정하며, items로 방문지 목록을 전달합니다.",
            operationId = "createCourse",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseCreateRequest.class),
                            examples = @ExampleObject(value = ApiExamples.COURSE_CREATE_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "코스 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class),
                            examples = @ExampleObject(value = ApiExamples.COURSE_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 필드 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ApiResponse<CourseResponse> create(
            CourseCreateRequest request,
            @Parameter(hidden = true) Long authenticatedMemberId
    );

    @Operation(
            summary = "코스 수정",
            description = "코스 제목, 지역, 날짜, 방문지 목록을 수정합니다. 본인 코스만 수정 가능합니다.",
            operationId = "updateCourse",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseUpdateRequest.class),
                            examples = @ExampleObject(value = ApiExamples.COURSE_UPDATE_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "코스 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class),
                            examples = @ExampleObject(value = ApiExamples.COURSE_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 필드 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 코스가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스 없음")
    })
    ApiResponse<CourseResponse> update(
            @Parameter(description = "코스 ID", example = "c1") @NotBlank String id,
            CourseUpdateRequest request,
            @Parameter(hidden = true) Long authenticatedMemberId
    );

    @Operation(
            summary = "코스 방문 순서 AI 추천",
            description = "AI가 코스 방문지 순서를 최적화합니다. 현재 위치를 전달하면 출발지 기반으로 최적화합니다.",
            operationId = "recommendCourseOrder",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = false,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseOrderRecommendationRequest.class),
                            examples = @ExampleObject(value = ApiExamples.COURSE_ORDER_RECOMMENDATION_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "순서 추천 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class),
                            examples = @ExampleObject(value = ApiExamples.COURSE_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 코스가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스 없음")
    })
    ApiResponse<CourseResponse> recommendOrder(
            @Parameter(description = "코스 ID", example = "c1") @NotBlank String id,
            CourseOrderRecommendationRequest request,
            @Parameter(hidden = true) Long authenticatedMemberId
    );

    @Operation(
            summary = "코스 저장(찜)",
            description = "코스를 내 저장 목록에 추가합니다.",
            operationId = "saveCourse",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "코스 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스 없음")
    })
    ApiResponse<Void> save(
            @Parameter(description = "코스 ID", example = "c1") @NotBlank String id,
            @Parameter(hidden = true) Long authenticatedMemberId
    );

    @Operation(
            summary = "코스 저장 취소",
            description = "코스를 내 저장 목록에서 제거합니다.",
            operationId = "unsaveCourse",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "코스 저장 취소 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스 없음")
    })
    ApiResponse<Void> unsave(
            @Parameter(description = "코스 ID", example = "c1") @NotBlank String id,
            @Parameter(hidden = true) Long authenticatedMemberId
    );

    @Operation(
            summary = "코스 삭제",
            description = "코스를 삭제합니다. 본인 코스만 삭제 가능합니다.",
            operationId = "deleteCourse",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "코스 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 코스가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "코스 없음")
    })
    ApiResponse<Void> delete(
            @Parameter(description = "코스 ID", example = "c1") @NotBlank String id,
            @Parameter(hidden = true) Long authenticatedMemberId
    );
}
