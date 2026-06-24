package com.ssafy.enjoytrip.core.api.web.controller;

import static com.ssafy.enjoytrip.core.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.core.api.security.AuthenticatedMemberId;
import com.ssafy.enjoytrip.core.api.web.api.MemberApi;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberLoginRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberOAuthSignupRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberSignupRequest;
import com.ssafy.enjoytrip.core.api.web.dto.request.MemberUpdateRequest;
import com.ssafy.enjoytrip.core.api.web.dto.response.LoginResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.UserEnvelopeResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.UserResponse;
import com.ssafy.enjoytrip.core.api.web.dto.response.UsersResponse;
import com.ssafy.enjoytrip.core.domain.Member;
import com.ssafy.enjoytrip.core.domain.service.MemberService;
import com.ssafy.enjoytrip.core.support.auth.IssuedToken;
import com.ssafy.enjoytrip.core.support.auth.JwtTokenService;
import com.ssafy.enjoytrip.core.support.auth.OAuthSignupTicketService;
import com.ssafy.enjoytrip.core.support.auth.OAuthSignupTicketService.PendingOAuthSignup;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
        List<UserResponse> users = service.findAllUsers()
                .stream()
                .map(MemberController::toUserResponse)
                .toList();
        return success(new UsersResponse(users));
    }

    @PostMapping
    @Override
    public ApiResponse<Void> signup(@Valid @RequestBody MemberSignupRequest request) {
        service.signup(new Member(
                null,
                request.name().trim(),
                request.nicknameOrName(),
                request.email(),
                request.password(),
                request.normalizedProfileImageUrl()
        ));
        return success();
    }

    @PostMapping("/login")
    @Override
    public ApiResponse<LoginResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        Member member = service.login(request.email(), request.password());
        IssuedToken token = tokenService.issue(member);
        return success(new LoginResponse(
                toUserResponse(member),
                token.accessToken(),
                token.tokenType(),
                token.expiresIn()
        ));
    }

    @PostMapping("/oauth")
    @Override
    public ApiResponse<LoginResponse> completeOAuthSignup(
            @Valid @RequestBody MemberOAuthSignupRequest request
    ) {
        PendingOAuthSignup pending = oauthSignupTicketService.verify(request.oauthSignupTicket().trim());
        Member member = service.signupWithOAuth(
                pending.provider(),
                pending.providerSubject(),
                pending.email(),
                request.name().trim(),
                request.nickname().trim()
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
    public ApiResponse<Void> logout(@AuthenticatedMemberId Long memberId) {
        service.logout(memberId);
        return success();
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserEnvelopeResponse> me(@AuthenticatedMemberId Long memberId) {
        Member member = service.findRequiredById(memberId);
        return success(new UserEnvelopeResponse(toUserResponse(member)));
    }

    @PutMapping("/me")
    @Override
    public ApiResponse<Void> updateMe(
            @Valid @RequestBody MemberUpdateRequest request,
            @AuthenticatedMemberId Long memberId
    ) {
        service.update(new Member(
                memberId,
                null,
                request.normalizedNickname(),
                null,
                null,
                null
        ));
        return success();
    }

    @DeleteMapping("/me")
    @Override
    public ApiResponse<Void> deleteMe(@AuthenticatedMemberId Long memberId) {
        service.delete(memberId);
        return success();
    }

    private static UserResponse toUserResponse(Member member) {
        return new UserResponse(
                member.name(),
                member.nickname(),
                member.email(),
                member.profileImageUrl()
        );
    }
}
