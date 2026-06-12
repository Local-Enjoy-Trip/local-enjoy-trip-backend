package com.ssafy.enjoytrip.web.controller;

import static com.ssafy.enjoytrip.support.error.ErrorType.ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.AUTHENTICATION_REQUIRED;
import static com.ssafy.enjoytrip.support.error.ErrorType.PASSWORD_LOOKUP_GONE;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.service.JwtTokenService;
import com.ssafy.enjoytrip.service.MemberService;
import com.ssafy.enjoytrip.service.OAuthSignupTicketService;
import com.ssafy.enjoytrip.service.OAuthSignupTicketService.PendingOAuthSignup;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.api.MemberApi;
import com.ssafy.enjoytrip.web.dto.request.MemberLoginRequest;
import com.ssafy.enjoytrip.web.dto.request.MemberLogoutRequest;
import com.ssafy.enjoytrip.web.dto.request.MemberOAuthSignupRequest;
import com.ssafy.enjoytrip.web.dto.request.MemberSignupRequest;
import com.ssafy.enjoytrip.web.dto.request.MemberUpdateRequest;
import com.ssafy.enjoytrip.web.dto.response.IssuedToken;
import com.ssafy.enjoytrip.web.dto.response.LoginResponse;
import com.ssafy.enjoytrip.web.dto.response.UserEnvelopeResponse;
import com.ssafy.enjoytrip.web.dto.response.UserResponse;
import com.ssafy.enjoytrip.web.dto.response.UsersResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
        throw new CoreException(PASSWORD_LOOKUP_GONE);
    }

    @PutMapping("/{userId}")
    @Override
    public ApiResponse<Void> update(@PathVariable String userId,
                                    @Valid @RequestBody MemberUpdateRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return updateAuthenticated(userId, request, jwt);
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserEnvelopeResponse> me(@AuthenticationPrincipal Jwt jwt) {
        String userId = authenticatedUserId(jwt);
        Member member = service.findRequiredByUserId(userId);
        return success(new UserEnvelopeResponse(toUserResponse(member)));
    }

    @PutMapping("/me")
    @Override
    public ApiResponse<Void> updateMe(@Valid @RequestBody MemberUpdateRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        String userId = authenticatedUserId(jwt);
        return updateAuthenticated(userId, request, jwt);
    }

    @DeleteMapping("/me")
    @Override
    public ApiResponse<Void> deleteMe(@AuthenticationPrincipal Jwt jwt) {
        String userId = authenticatedUserId(jwt);
        return deleteAuthenticated(userId, jwt);
    }

    private ApiResponse<Void> updateAuthenticated(String userId,
                                                  MemberUpdateRequest request,
                                                  Jwt jwt) {
        authorizeUser(userId, jwt);
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
    public ApiResponse<Void> delete(@PathVariable String userId, @AuthenticationPrincipal Jwt jwt) {
        return deleteAuthenticated(userId, jwt);
    }

    private ApiResponse<Void> deleteAuthenticated(String userId, Jwt jwt) {
        authorizeUser(userId, jwt);
        service.delete(userId);
        return success();
    }

    private void authorizeUser(String userId, Jwt jwt) {
        if (jwt == null) {
            throw new CoreException(AUTHENTICATION_REQUIRED);
        }
        String authenticatedUserId = authenticatedUserId(jwt);
        if (!authenticatedUserId.equals(userId)) {
            throw new CoreException(ACCESS_DENIED);
        }
    }

    private String authenticatedUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new CoreException(AUTHENTICATION_REQUIRED);
        }
        return jwt.getSubject();
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
