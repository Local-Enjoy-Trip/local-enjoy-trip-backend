package com.ssafy.enjoytrip.web.controller;

import com.ssafy.enjoytrip.web.api.*;

import static com.ssafy.enjoytrip.support.error.ErrorType.ACCESS_DENIED;
import static com.ssafy.enjoytrip.support.error.ErrorType.AUTHENTICATION_REQUIRED;
import static com.ssafy.enjoytrip.support.error.ErrorType.EMAIL_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_CREDENTIALS;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_EMAIL;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_LATITUDE_OR_LONGITUDE;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_NAME;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_NICKNAME;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_PASSWORD;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_PROFILE_IMAGE_URL;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_REQUEST;
import static com.ssafy.enjoytrip.support.error.ErrorType.INVALID_USER_ID;
import static com.ssafy.enjoytrip.support.error.ErrorType.MISSING_REQUIRED_FIELDS;
import static com.ssafy.enjoytrip.support.error.ErrorType.MISSING_USER_ID;
import static com.ssafy.enjoytrip.support.error.ErrorType.PASSWORD_LOOKUP_GONE;
import static com.ssafy.enjoytrip.support.error.ErrorType.USER_ALREADY_EXISTS;
import static com.ssafy.enjoytrip.support.error.ErrorType.USER_NOT_FOUND;
import static com.ssafy.enjoytrip.support.response.ApiResponse.success;

import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.service.JwtTokenService;
import com.ssafy.enjoytrip.service.MemberService;
import com.ssafy.enjoytrip.service.OAuthSignupTicketService;
import com.ssafy.enjoytrip.service.OAuthSignupTicketService.PendingOAuthSignup;
import com.ssafy.enjoytrip.support.error.CoreException;
import com.ssafy.enjoytrip.support.error.ErrorType;
import com.ssafy.enjoytrip.support.response.ApiResponse;
import com.ssafy.enjoytrip.web.dto.response.IssuedToken;
import com.ssafy.enjoytrip.web.dto.response.LoginResponse;
import com.ssafy.enjoytrip.web.dto.request.MemberRequest;
import com.ssafy.enjoytrip.web.dto.response.UserEnvelopeResponse;
import com.ssafy.enjoytrip.web.dto.response.UserResponse;
import com.ssafy.enjoytrip.web.dto.response.UsersResponse;
import java.util.List;
import java.util.regex.Pattern;
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
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_]{4,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,64}$");

    private final MemberService service;
    private final JwtTokenService tokenService;
    private final OAuthSignupTicketService oauthSignupTicketService;

    @GetMapping
    @Override
    public ApiResponse<UsersResponse> findAll() {
        List<UserResponse> users = service.findAllUsers().stream().map(MemberController::toUserResponse).toList();
        return success(new UsersResponse(users));
    }

    @PostMapping("/signup")
    @Override
    public ApiResponse<Void> signup(@RequestBody(required = false) MemberRequest request) {
        request = requestOrEmpty(request);
        String userId = trim(request.userId());
        String name = trim(request.name());
        String nickname = trim(request.nickname());
        String email = trim(request.email());
        String password = trim(request.password());
        if (userId.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return fail(MISSING_REQUIRED_FIELDS);
        }
        validateUserId(userId);
        validateName(name);
        if (nickname.isEmpty()) {
            nickname = name;
        }
        validateNickname(nickname);
        validateEmail(email);
        validatePassword(password);
        validateProfileImageUrl(request.profileImageUrl());
        validateLocation(request.representativeLatitude(), request.representativeLongitude());
        validateRepresentativeRegionName(request.representativeRegionName());
        if (service.existsByUserId(userId)) {
            return fail(USER_ALREADY_EXISTS);
        }
        if (service.existsByEmail(email)) {
            return fail(EMAIL_ALREADY_EXISTS);
        }
        if (service.signup(new Member(
                userId,
                name,
                nickname,
                email,
                password,
                trimToNull(request.profileImageUrl()),
                request.representativeLatitude(),
                request.representativeLongitude(),
                trimToNull(request.representativeRegionName()),
                ""
        ))) {
            return success();
        }
        return fail(USER_ALREADY_EXISTS);
    }

    @PostMapping("/login")
    @Override
    public ApiResponse<LoginResponse> login(@RequestBody(required = false) MemberRequest request) {
        request = requestOrEmpty(request);
        String userId = trim(request.userId());
        String password = trim(request.password());
        if (userId.isEmpty() || password.isEmpty()) {
            return fail(INVALID_CREDENTIALS);
        }
        Member member = service.login(userId, password);
        if (member == null) {
            return fail(INVALID_CREDENTIALS);
        }
        IssuedToken token = tokenService.issue(member);
        return success(new LoginResponse(
                toUserResponse(member),
                token.accessToken(),
                token.tokenType(),
                token.expiresIn()
        ));
    }

    @PostMapping("/oauth/signup")
    public ApiResponse<LoginResponse> completeOAuthSignup(@RequestBody(required = false) MemberRequest request) {
        request = requestOrEmpty(request);
        String ticket = trim(request.oauthSignupTicket());
        String name = trim(request.name());
        if (ticket.isEmpty() || name.isEmpty()) {
            return fail(MISSING_REQUIRED_FIELDS);
        }
        validateName(name);

        PendingOAuthSignup pending;
        try {
            pending = oauthSignupTicketService.verify(ticket);
        } catch (RuntimeException exception) {
            return fail(ErrorType.INVALID_REQUEST);
        }
        Member member = service.signupWithOAuth(pending.provider(), pending.providerUserId(), pending.email(), name);
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
    public ApiResponse<Void> logout(@RequestBody(required = false) MemberRequest request) {
        request = requestOrEmpty(request);
        String userId = trim(request.userId());
        if (userId.isEmpty()) {
            return fail(MISSING_USER_ID);
        }
        service.logout(userId);
        return success();
    }

    @PostMapping("/find-password")
    @Override
    public ApiResponse<Void> findPassword(@RequestBody(required = false) MemberRequest request) {
        return fail(PASSWORD_LOOKUP_GONE);
    }

    @PutMapping("/{userId}")
    @Override
    public ApiResponse<Void> update(@PathVariable String userId,
                                    @RequestBody(required = false) MemberRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return updateAuthenticated(userId, request, jwt);
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserEnvelopeResponse> me(@AuthenticationPrincipal Jwt jwt) {
        String userId = authenticatedUserId(jwt);
        Member member = service.findByUserId(userId);
        if (member == null) {
            return fail(USER_NOT_FOUND);
        }
        return success(new UserEnvelopeResponse(toUserResponse(member)));
    }

    @PutMapping("/me")
    @Override
    public ApiResponse<Void> updateMe(@RequestBody(required = false) MemberRequest request,
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
                                                  MemberRequest request,
                                                  Jwt jwt) {
        request = requestOrEmpty(request);
        if (trim(userId).isEmpty()) {
            return fail(MISSING_USER_ID);
        }
        authorizeUser(userId, jwt);
        String name = trim(request.name());
        String nickname = trim(request.nickname());
        String email = trim(request.email());
        String password = trim(request.password());
        if (!name.isEmpty()) {
            validateName(name);
        }
        if (!nickname.isEmpty()) {
            validateNickname(nickname);
        }
        if (!email.isEmpty()) {
            validateEmail(email);
            Member owner = service.findByEmail(email);
            if (owner != null && !owner.userId().equals(userId)) {
                return fail(EMAIL_ALREADY_EXISTS);
            }
        }
        if (!password.isEmpty()) {
            validatePassword(password);
        }
        validateProfileImageUrl(request.profileImageUrl());
        validateLocation(request.representativeLatitude(), request.representativeLongitude());
        validateRepresentativeRegionName(request.representativeRegionName());

        if (service.update(new Member(
                userId,
                name,
                nickname,
                email,
                password,
                trimToNull(request.profileImageUrl()),
                request.representativeLatitude(),
                request.representativeLongitude(),
                trimToNull(request.representativeRegionName()),
                ""
        ))) {
            return success();
        }
        return fail(USER_NOT_FOUND);
    }

    @DeleteMapping("/{userId}")
    @Override
    public ApiResponse<Void> delete(@PathVariable String userId, @AuthenticationPrincipal Jwt jwt) {
        return deleteAuthenticated(userId, jwt);
    }

    private ApiResponse<Void> deleteAuthenticated(String userId, Jwt jwt) {
        if (trim(userId).isEmpty()) {
            return fail(MISSING_USER_ID);
        }
        authorizeUser(userId, jwt);
        if (service.delete(userId)) {
            return success();
        }
        return fail(USER_NOT_FOUND);
    }

    private void authorizeUser(String userId, Jwt jwt) {
        if (jwt == null) {
            fail(AUTHENTICATION_REQUIRED);
        }
        String authenticatedUserId = authenticatedUserId(jwt);
        if (!authenticatedUserId.equals(userId)) {
            fail(ACCESS_DENIED);
        }
    }

    private String authenticatedUserId(Jwt jwt) {
        return trim(jwt.getSubject());
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

    private static <T> T fail(ErrorType error) {
        throw new CoreException(error);
    }

    private static MemberRequest requestOrEmpty(MemberRequest request) {
        if (request != null) {
            return request;
        }
        return new MemberRequest(null, null, null, null, null, null, null, null, null, null, null);
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static void validateUserId(String userId) {
        if (!USER_ID_PATTERN.matcher(userId).matches()) {
            fail(INVALID_USER_ID);
        }
    }

    private static void validateName(String name) {
        if (name.length() < 2 || name.length() > 30) {
            fail(INVALID_NAME);
        }
    }

    private static void validateNickname(String nickname) {
        if (nickname.length() < 2 || nickname.length() > 30) {
            fail(INVALID_NICKNAME);
        }
    }

    private static void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            fail(INVALID_EMAIL);
        }
    }

    private static void validatePassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            fail(INVALID_PASSWORD);
        }
    }

    private static void validateProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl != null && profileImageUrl.length() > 512) {
            fail(INVALID_PROFILE_IMAGE_URL);
        }
    }

    private static void validateLocation(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return;
        }
        if (latitude == null || longitude == null) {
            fail(INVALID_LATITUDE_OR_LONGITUDE);
        }
        if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0) {
            fail(INVALID_LATITUDE_OR_LONGITUDE);
        }
    }

    private static void validateRepresentativeRegionName(String regionName) {
        if (regionName != null && regionName.length() > 100) {
            fail(INVALID_REQUEST);
        }
    }

    private static String trimToNull(String value) {
        String trimmed = trim(value);
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private static String value(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        return value;
    }

}
