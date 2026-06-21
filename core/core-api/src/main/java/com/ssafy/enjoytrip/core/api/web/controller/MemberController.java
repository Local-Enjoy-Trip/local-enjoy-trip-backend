package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.service.JwtTokenService;
import com.ssafy.enjoytrip.core.domain.service.MemberService;
import com.ssafy.enjoytrip.core.domain.service.OAuthSignupTicketService;
import com.ssafy.enjoytrip.core.domain.service.OAuthSignupTicketService.PendingOAuthSignup;
import com.ssafy.enjoytrip.core.support.error.exception.DeprecatedEndpointException;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import com.ssafy.enjoytrip.core.api.web.api.MemberApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberLoginRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberLogoutRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberOAuthSignupRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberSignupRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.IssuedToken;
import com.ssafy.enjoytrip.core.api.web.dto.response.LoginResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.UserEnvelopeResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.UserResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.UsersResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.ssafy.enjoytrip.core.api.security.AuthenticatedUserId;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController implements MemberApi {
    private final MemberService service;
    private final JwtTokenService tokenService;
    private final OAuthSignupTicketService oauthSignupTicketService;

    @GetMapping
    @Override
    public ApiResponse<UsersResponse> findAll() {
        List<UserResponse> users = service.findAllUsers().stream().map(MemberController::toUserResponse).toList();
        return success(new UsersResponse(users));
    }

    @PostMapping
    @Override
    public ApiResponse<Void> signup(@Valid @RequestBody MemberSignupRequest request) {
        service.signup(new Member(
                request.userId(),
                request.name().trim(),
                request.nicknameOrName(),
                request.email().trim(),
                request.password(),
                request.normalizedProfileImageUrl(),
                request.representativeLatitude(),
                request.representativeLongitude(),
                request.normalizedRepresentativeRegionName(),
                ""
        ));
        return success();
    }

    @PostMapping("/login")
    @Override
    public ApiResponse<LoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        Member member = service.login(request.userId(), request.password());
        IssuedToken token = tokenService.issue(member);
        return success(new LoginResponse(
                toUserResponse(member),
                token.accessToken(),
                token.tokenType(),
                token.expiresIn()
        ));
    }

    @PostMapping("/oauth")
    public ApiResponse<LoginResponse> completeOAuthSignup(@Valid @RequestBody MemberOAuthSignupRequest request) {
        PendingOAuthSignup pending = oauthSignupTicketService.verify(request.oauthSignupTicket().trim());
        Member member = service.signupWithOAuth(
                pending.provider(),
                pending.providerUserId(),
                pending.email(),
                request.name().trim()
        );
        IssuedToken token = tokenService.issue(member);
        return success(new LoginResponse(
                toUserResponse(member),
                token.accessToken(),
                token.tokenType(),
                token.expiresIn()
        ));
    }

    @PostMapping("/logout")
    @Override
    public ApiResponse<Void> logout(@Valid @RequestBody MemberLogoutRequest request) {
        service.logout(request.userId());
        return success();
    }

    @PostMapping("/password-lookup-requests")
    @Override
    public ApiResponse<Void> findPassword() {
        throw new DeprecatedEndpointException(
                "비밀번호 찾기는 더 이상 지원하지 않습니다. 비밀번호를 재설정하세요."
        );
    }

    @PutMapping("/{userId}")
    @Override
    public ApiResponse<Void> update(
            @PathVariable String userId,
            @Valid @RequestBody MemberUpdateRequest request,
            @AuthenticatedUserId String authenticatedUserId
    ) {
        return updateAuthenticated(userId, request, authenticatedUserId);
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserEnvelopeResponse> me(@AuthenticatedUserId String authenticatedUserId) {
        Member member = service.findRequiredByUserId(authenticatedUserId);
        return success(new UserEnvelopeResponse(toUserResponse(member)));
    }

    @PutMapping("/me")
    @Override
    public ApiResponse<Void> updateMe(
            @Valid @RequestBody MemberUpdateRequest request,
            @AuthenticatedUserId String authenticatedUserId
    ) {
        return updateAuthenticated(authenticatedUserId, request, authenticatedUserId);
    }

    @DeleteMapping("/me")
    @Override
    public ApiResponse<Void> deleteMe(@AuthenticatedUserId String authenticatedUserId) {
        return deleteAuthenticated(authenticatedUserId, authenticatedUserId);
    }

    private ApiResponse<Void> updateAuthenticated(String userId,
                                                  MemberUpdateRequest request,
                                                  String authenticatedUserId) {
        authorizeUser(userId, authenticatedUserId);
        service.update(new Member(
                userId,
                request.normalizedName(),
                request.normalizedNickname(),
                request.normalizedEmail(),
                request.normalizedPassword(),
                request.normalizedProfileImageUrl(),
                request.representativeLatitude(),
                request.representativeLongitude(),
                request.normalizedRepresentativeRegionName(),
                ""
        ));
        return success();
    }

    @DeleteMapping("/{userId}")
    @Override
    public ApiResponse<Void> delete(@PathVariable String userId, @AuthenticatedUserId String authenticatedUserId) {
        return deleteAuthenticated(userId, authenticatedUserId);
    }

    private ApiResponse<Void> deleteAuthenticated(String userId, String authenticatedUserId) {
        authorizeUser(userId, authenticatedUserId);
        service.delete(userId);
        return success();
    }

    private void authorizeUser(String userId, String authenticatedUserId) {
        service.requireSameUser(userId, authenticatedUserId);
    }

    private static UserResponse toUserResponse(Member member) {
        return new UserResponse(
                member.userId(),
                member.name(),
                value(member.nickname(), member.name()),
                member.email(),
                member.profileImageUrl(),
                member.representativeLatitude(),
                member.representativeLongitude(),
                member.representativeRegionName(),
                value(member.createdAt(), "")
        );
    }

    private static String value(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        return value;
    }

}
