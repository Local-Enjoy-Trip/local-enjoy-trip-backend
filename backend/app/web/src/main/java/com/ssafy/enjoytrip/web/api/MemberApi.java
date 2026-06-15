package com.ssafy.enjoytrip.web.api;

import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.request.MemberLoginRequest;
import com.ssafy.enjoytrip.web.dto.request.MemberLogoutRequest;
import com.ssafy.enjoytrip.web.dto.request.MemberSignupRequest;
import com.ssafy.enjoytrip.web.dto.request.MemberUpdateRequest;
import com.ssafy.enjoytrip.web.dto.response.LoginResponse;
import com.ssafy.enjoytrip.web.dto.response.UserEnvelopeResponse;
import com.ssafy.enjoytrip.web.dto.response.UsersResponse;
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
                                          "profileImageUrl": "https://cdn.example.com/profile.png",
                                          "representativeLatitude": 37.5665,
                                          "representativeLongitude": 126.978,
                                          "representativeRegionName": "서울 중구",
                                          "createdAt": "2026-05-20"
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
            description = "`userId`, `name`, `email`, `password`를 등록하고 선택적으로 닉네임, 프로필 이미지, 대표 위치를 받습니다.",
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
                                          "profileImageUrl": "https://cdn.example.com/profile.png",
                                          "representativeLatitude": 37.5665,
                                          "representativeLongitude": 126.978,
                                          "representativeRegionName": "서울 중구",
                                          "createdAt": "2026-05-20"
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

    @Operation(summary = "로그아웃", description = "`userId` 기준으로 로그아웃 처리를 수행합니다.", operationId = "logout")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "userId 누락")
    })
    ApiResponse<Void> logout(MemberLogoutRequest request);

    @Operation(summary = "비밀번호 찾기 종료 안내", description = "비밀번호 찾기 기능은 더 이상 지원하지 않아 410 Gone을 반환합니다.", operationId = "findPassword")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "비밀번호 찾기 기능 제거")
    })
    ApiResponse<Void> findPassword();

    @Operation(
            summary = "회원 수정",
            description = "경로의 `userId` 회원 정보와 닉네임, 프로필 이미지, 대표 위치를 수정합니다. 인증된 사용자 본인만 수정할 수 있습니다.",
            operationId = "updateMember",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "userId 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 사용자 계정 접근"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    ApiResponse<Void> update(
            @Parameter(description = "수정할 회원 ID", example = "ssafy", required = true) String userId,
            MemberUpdateRequest request,
            @Parameter(hidden = true) String authenticatedUserId
    );

    @Operation(summary = "내 정보 조회", description = "JWT subject에 해당하는 회원 정보를 조회합니다.", operationId = "me", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 정보 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserEnvelopeResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    ApiResponse<UserEnvelopeResponse> me(@Parameter(hidden = true) String authenticatedUserId);

    @Operation(
            summary = "내 정보 수정",
            description = "JWT subject에 해당하는 내 회원 정보와 닉네임, 프로필 이미지, 대표 위치를 수정합니다.",
            operationId = "updateMe",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 정보 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 사용자 계정 접근"),
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

    @Operation(summary = "회원 삭제", description = "경로의 `userId` 회원을 삭제합니다. 인증된 사용자 본인만 삭제할 수 있습니다.", operationId = "deleteMember", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "userId 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 사용자 계정 접근"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 없음")
    })
    ApiResponse<Void> delete(
            @Parameter(description = "삭제할 회원 ID", example = "ssafy", required = true) String userId,
            @Parameter(hidden = true) String authenticatedUserId
    );
}
