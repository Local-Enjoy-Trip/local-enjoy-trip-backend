package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.api.web.dto.request.MemberLoginRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberOAuthSignupRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberSignupRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.LoginResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.UserEnvelopeResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.UsersResponse;
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
import jakarta.validation.Valid;

@Tag(name = "Members", description = "회원 가입, 로그인, 내 정보, 회원 관리 API")
public interface MemberApi {

    @Operation(
            summary = "회원 목록 조회",
            description = "등록된 회원 목록을 조회합니다. 비밀번호와 내부 회원 ID는 응답하지 않습니다.",
            operationId = "findMembers"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsersResponse.class),
                            examples = @ExampleObject(value = ApiExamples.USERS_RESPONSE))
            )
    })
    ApiResponse<UsersResponse> findAll();

    @Operation(
            summary = "회원 가입",
            description = "`name`, `email`, `password`를 등록하고 선택적으로 닉네임과 프로필 이미지를 받습니다.",
            operationId = "signup",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberSignupRequest.class),
                            examples = @ExampleObject(value = ApiExamples.MEMBER_SIGNUP_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원 가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "필수 필드 누락 또는 지원하지 않는 필드"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 회원"
            )
    })
    ApiResponse<Void> signup(@Valid MemberSignupRequest request);

    @Operation(
            summary = "로그인",
            description = "정확히 일치하는 `email`, `password`로 인증하고 JWT access token을 발급합니다.",
            operationId = "login",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberLoginRequest.class),
                            examples = @ExampleObject(value = ApiExamples.MEMBER_LOGIN_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(value = ApiExamples.LOGIN_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "이메일 또는 비밀번호 불일치"
            )
    })
    ApiResponse<LoginResponse> login(@Valid MemberLoginRequest request);

    @Operation(
            summary = "OAuth 회원가입 완료",
            description = "OAuth 로그인 콜백에서 받은 가입 티켓과 이름, 닉네임으로 회원을 생성하고 JWT access token을 발급합니다.",
            operationId = "completeOAuthSignup",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberOAuthSignupRequest.class),
                            examples = @ExampleObject(value = ApiExamples.MEMBER_OAUTH_SIGNUP_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OAuth 회원가입 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(value = ApiExamples.OAUTH_LOGIN_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "필수 필드 누락 또는 유효하지 않은 가입 티켓"
            )
    })
    ApiResponse<LoginResponse> completeOAuthSignup(@Valid MemberOAuthSignupRequest request);

    @Operation(
            summary = "로그아웃",
            description = "Authorization 헤더의 JWT subject 회원을 기준으로 로그아웃 처리를 수행합니다.",
            operationId = "logout",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ApiResponse<Void> logout(@Parameter(hidden = true) Long memberId);

    @Operation(
            summary = "내 정보 조회",
            description = "JWT subject의 내부 회원 ID에 해당하는 회원 정보를 조회합니다.",
            operationId = "me",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserEnvelopeResponse.class),
                            examples = @ExampleObject(value = ApiExamples.USER_ENVELOPE_RESPONSE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    ApiResponse<UserEnvelopeResponse> me(@Parameter(hidden = true) Long memberId);

    @Operation(
            summary = "내 정보 수정",
            description = "JWT subject 회원의 닉네임을 수정합니다. 프로필 이미지는 전용 profile-image API에서 수정합니다.",
            operationId = "updateMe",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberUpdateRequest.class),
                            examples = @ExampleObject(value = ApiExamples.MEMBER_UPDATE_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 정보 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    ApiResponse<Void> updateMe(
            @Valid MemberUpdateRequest request,
            @Parameter(hidden = true) Long memberId
    );

    @Operation(
            summary = "내 계정 삭제",
            description = "JWT subject 회원의 내 계정을 삭제합니다.",
            operationId = "deleteMe",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 계정 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = ApiExamples.SUCCESS_VOID)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    ApiResponse<Void> deleteMe(@Parameter(hidden = true) Long memberId);
}
