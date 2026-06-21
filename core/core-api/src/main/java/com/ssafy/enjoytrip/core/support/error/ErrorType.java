package com.ssafy.enjoytrip.core.support.error;

import static com.ssafy.enjoytrip.core.support.error.LogLevel.WARN;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    USER_NOT_FOUND(ErrorCode.M001, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.", WARN),
    USER_ALREADY_EXISTS(ErrorCode.M002, HttpStatus.CONFLICT, "이미 존재하는 사용자입니다.", WARN),
    INVALID_CREDENTIALS(
            ErrorCode.M004,
            HttpStatus.UNAUTHORIZED,
            "아이디 또는 비밀번호가 올바르지 않습니다.",
            WARN
    ),
    MEMBER_REPRESENTATIVE_LOCATION_REQUIRED(
            ErrorCode.M005,
            HttpStatus.BAD_REQUEST,
            "대표 동네 위치를 먼저 설정하세요.",
            WARN
    ),

    TAG_NOT_FOUND(ErrorCode.A002, HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다.", WARN),
    TAG_ALREADY_EXISTS(ErrorCode.A003, HttpStatus.CONFLICT, "이미 존재하는 태그입니다.", WARN),

    PLAN_NOT_FOUND(ErrorCode.P001, HttpStatus.NOT_FOUND, "여행 계획을 찾을 수 없습니다.", WARN),
    PLAN_ACCESS_DENIED(
            ErrorCode.P002,
            HttpStatus.FORBIDDEN,
            "다른 사용자의 여행 계획에 접근할 수 없습니다.",
            WARN
    ),

    FRIENDSHIP_NOT_FOUND(ErrorCode.F001, HttpStatus.NOT_FOUND, "친구 관계를 찾을 수 없습니다.", WARN),
    FRIENDSHIP_SELF_REQUEST(
            ErrorCode.F002,
            HttpStatus.BAD_REQUEST,
            "자기 자신에게 친구 요청을 보낼 수 없습니다.",
            WARN
    ),
    FRIENDSHIP_INVALID_STATE(
            ErrorCode.F003,
            HttpStatus.CONFLICT,
            "현재 친구 관계 상태에서는 이 작업을 수행할 수 없습니다.",
            WARN
    ),
    FRIENDSHIP_ALREADY_ACTIVE(
            ErrorCode.F004,
            HttpStatus.CONFLICT,
            "이미 활성 친구 관계 또는 대기 중인 요청이 있습니다.",
            WARN
    ),
    FRIENDSHIP_ACCESS_DENIED(
            ErrorCode.F005,
            HttpStatus.FORBIDDEN,
            "다른 사용자의 친구 관계에 접근할 수 없습니다.",
            WARN
    ),

    NOTICE_NOT_FOUND(ErrorCode.N001, HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다.", WARN),
    NOTE_NOT_FOUND(ErrorCode.N002, HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다.", WARN),
    NOTE_NOT_ACTIVE(ErrorCode.N003, HttpStatus.GONE, "활성 상태의 노트가 아닙니다.", WARN),
    NOTE_ACCESS_DENIED(ErrorCode.N004, HttpStatus.FORBIDDEN, "다른 사용자의 노트에 접근할 수 없습니다.", WARN),
    NOTIFICATION_NOT_FOUND(ErrorCode.N005, HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.", WARN),
    NOTIFICATION_ACCESS_DENIED(
            ErrorCode.N006,
            HttpStatus.FORBIDDEN,
            "다른 사용자의 알림에 접근할 수 없습니다.",
            WARN
    ),

    POST_NOT_FOUND(ErrorCode.B001, HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.", WARN),

    HOTPLACE_NOT_FOUND(ErrorCode.H001, HttpStatus.NOT_FOUND, "핫플레이스를 찾을 수 없습니다.", WARN);

    private final ErrorCode code;
    private final HttpStatus status;
    private final String message;
    private final LogLevel logLevel;

    ErrorType(ErrorCode code, HttpStatus status, String message, LogLevel logLevel) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.logLevel = logLevel;
    }

    public ErrorCode code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }

    public LogLevel logLevel() {
        return logLevel;
    }
}
