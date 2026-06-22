package com.ssafy.enjoytrip.core.api.web.api;

import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberLoginRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberLogoutRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberOAuthSignupRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberSignupRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.LoginResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.UserEnvelopeResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.UsersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Members", description = "회원 가입, 로그인, 내 정보, 회원 관리 API")
public interface MemberApi {

    @Operation(summary = "회원 목록 조회", description = "등록된 회원 목록을 조회합니다. 비밀번호는 응답하지 않습니다.", operationId = "findMembers")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원 목록 조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsersResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "users": [{
                                          "userId": "ssafy",
                                          "name": "김싸피",
                                          "nickname": "동네핀러",
                                          "email": "ssafy@example.com",
                                          "profileImageUrl": "https://cdn.example.com/profile.png"
                                        }]
                                      },
                                      "error": null
                                    }
                                    """))
            )
    })
    ApiResponse<UsersResponse> findAll();

    @Operation(
            summary = "회원 가입",
            description = "`userId`, `name`, `email`, `password`를 등록하고 선택적으로 닉네임과 프로필 이미지를 받습니다.",
            operationId = "signup"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "필수 필드 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 회원")
    })
    ApiResponse<Void> signup(MemberSignupRequest request);

    @Operation(summary = "로그인", description = "`userId`, `password`로 인증하고 JWT access token을 발급합니다.", operationId = "login")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "user": {
                                          "userId": "ssafy",
                                          "name": "김싸피",
                                          "nickname": "동네핀러",
                                          "email": "ssafy@example.com",
                                          "profileImageUrl": "https://cdn.example.com/profile.png"
                                        },
                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                        "tokenType": "Bearer",
                                        "expiresIn": 3600
                                      },
                                      "error": null
                                    }
                                    """))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치")
    })
    ApiResponse<LoginResponse> login(MemberLoginRequest request);

    @Operation(
            summary = "OAuth 회원가입 완료",
            description = "OAuth 로그인 콜백에서 받은 가입 티켓과 이름, 닉네임으로 회원을 생성하고 JWT access token을 발급합니다.",
            operationId = "completeOAuthSignup"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OAuth 회원가입 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "필수 필드 누락 또는 유효하지 않은 가입 티켓"
            )
    })
    ApiResponse<LoginResponse> completeOAuthSignup(MemberOAuthSignupRequest request);

    @Operation(summary = "로그아웃", description = "`userId` 기준으로 로그아웃 처리를 수행합니다.", operationId = "logout")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "userId 누락")
    })
    ApiResponse<Void> logout(MemberLogoutRequest request);

    @Operation(summary = "내 정보 조회", description = "JWT subject에 해당하는 회원 정보를 조회합니다.", operationId = "me", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 정보 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserEnvelopeResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    ApiResponse<UserEnvelopeResponse> me(@Parameter(hidden = true) String authenticatedUserId);

    @Operation(
            summary = "내 정보 수정",
            description = "JWT subject에 해당하는 회원의 닉네임과 프로필 이미지를 수정합니다.",
            operationId = "updateMe",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 정보 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    ApiResponse<Void> updateMe(MemberUpdateRequest request, @Parameter(hidden = true) String authenticatedUserId);

    @Operation(summary = "내 계정 삭제", description = "JWT subject에 해당하는 내 계정을 삭제합니다.", operationId = "deleteMe", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 계정 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 사용자 계정 접근"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    ApiResponse<Void> deleteMe(@Parameter(hidden = true) String authenticatedUserId);
}
