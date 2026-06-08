package com.ssafy.enjoytrip.web.dto.request;

public record MemberRequest(
    String action,
    String userId,
    String name,
    String email,
    String password,
    String oauthSignupTicket
) {}
