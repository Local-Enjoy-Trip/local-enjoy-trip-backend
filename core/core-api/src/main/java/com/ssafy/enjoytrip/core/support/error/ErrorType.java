package com.ssafy.enjoytrip.core.support.error;

import static com.ssafy.enjoytrip.core.support.error.ErrorCode.BAD_GATEWAY;
import static com.ssafy.enjoytrip.core.support.error.ErrorCode.BAD_REQUEST;
import static com.ssafy.enjoytrip.core.support.error.ErrorCode.CONFLICT;
import static com.ssafy.enjoytrip.core.support.error.ErrorCode.FORBIDDEN;
import static com.ssafy.enjoytrip.core.support.error.ErrorCode.GONE;
import static com.ssafy.enjoytrip.core.support.error.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.ssafy.enjoytrip.core.support.error.ErrorCode.METHOD_NOT_ALLOWED;
import static com.ssafy.enjoytrip.core.support.error.ErrorCode.NOT_FOUND;
import static com.ssafy.enjoytrip.core.support.error.ErrorCode.SERVICE_UNAVAILABLE;
import static com.ssafy.enjoytrip.core.support.error.ErrorCode.UNAUTHORIZED;
import static com.ssafy.enjoytrip.core.support.error.LogLevel.ERROR;
import static com.ssafy.enjoytrip.core.support.error.LogLevel.WARN;

public enum ErrorType {
    INVALID_ACTION(BAD_REQUEST, "유효하지 않은 작업입니다.", WARN),
    MISSING_REQUIRED_FIELDS(BAD_REQUEST, "필수 입력값이 누락되었습니다.", WARN),
    MISSING_ID(BAD_REQUEST, "id가 누락되었습니다.", WARN),
    MISSING_USER_ID(BAD_REQUEST, "userId가 누락되었습니다.", WARN),
    INVALID_REQUEST(BAD_REQUEST, "유효하지 않은 요청입니다.", WARN),
    INVALID_ID(BAD_REQUEST, "유효하지 않은 id입니다.", WARN),
    INVALID_POINTS(BAD_REQUEST, "유효하지 않은 경로 좌표입니다.", WARN),
    INVALID_LATITUDE_OR_LONGITUDE(BAD_REQUEST, "위도 또는 경도가 유효하지 않습니다.", WARN),
    INVALID_USER_ID(
            BAD_REQUEST,
            "사용자 ID는 4-20자의 영문, 숫자, 밑줄만 사용할 수 있습니다.",
            WARN
    ),
    INVALID_NAME(BAD_REQUEST, "이름은 2-30자여야 합니다.", WARN),
    INVALID_NICKNAME(BAD_REQUEST, "닉네임은 2-30자여야 합니다.", WARN),
    INVALID_EMAIL(BAD_REQUEST, "유효하지 않은 이메일입니다.", WARN),
    INVALID_PASSWORD(
            BAD_REQUEST,
            "비밀번호는 8-64자이며 영문과 숫자를 포함해야 합니다.",
            WARN
    ),
    INVALID_PROFILE_IMAGE_URL(BAD_REQUEST, "프로필 이미지 URL은 512자 이하여야 합니다.", WARN),
    MEMBER_REPRESENTATIVE_LOCATION_REQUIRED(
            BAD_REQUEST,
            "대표 동네 위치를 먼저 설정하세요.",
            WARN
    ),

    AUTHENTICATION_REQUIRED(UNAUTHORIZED, "인증이 필요합니다.", WARN),
    INVALID_CREDENTIALS(UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.", WARN),
    ACCESS_DENIED(FORBIDDEN, "다른 사용자의 계정에 접근할 수 없습니다.", WARN),

    USER_NOT_FOUND(NOT_FOUND, "사용자를 찾을 수 없습니다.", WARN),
    ATTRACTION_NOT_FOUND(NOT_FOUND, "관광지를 찾을 수 없습니다.", WARN),
    TAG_NOT_FOUND(NOT_FOUND, "태그를 찾을 수 없습니다.", WARN),
    POST_NOT_FOUND(NOT_FOUND, "게시글을 찾을 수 없습니다.", WARN),
    HOTPLACE_NOT_FOUND(NOT_FOUND, "핫플레이스를 찾을 수 없습니다.", WARN),
    PLAN_NOT_FOUND(NOT_FOUND, "여행 계획을 찾을 수 없습니다.", WARN),
    NOTICE_NOT_FOUND(NOT_FOUND, "공지사항을 찾을 수 없습니다.", WARN),
    NOTE_NOT_FOUND(NOT_FOUND, "노트를 찾을 수 없습니다.", WARN),
    NOTE_NOT_ACTIVE(GONE, "활성 상태의 노트가 아닙니다.", WARN),
    NOTE_ACCESS_DENIED(FORBIDDEN, "다른 사용자의 노트에 접근할 수 없습니다.", WARN),
    FRIENDSHIP_NOT_FOUND(NOT_FOUND, "친구 관계를 찾을 수 없습니다.", WARN),
    FRIENDSHIP_SELF_REQUEST(
            BAD_REQUEST,
            "자기 자신에게 친구 요청을 보낼 수 없습니다.",
            WARN
    ),
    FRIENDSHIP_INVALID_STATE(
            CONFLICT,
            "현재 친구 관계 상태에서는 이 작업을 수행할 수 없습니다.",
            WARN
    ),
    FRIENDSHIP_ALREADY_ACTIVE(
            CONFLICT,
            "이미 활성 친구 관계 또는 대기 중인 요청이 있습니다.",
            WARN
    ),
    FRIENDSHIP_ACCESS_DENIED(
            FORBIDDEN,
            "다른 사용자의 친구 관계에 접근할 수 없습니다.",
            WARN
    ),
    NOTIFICATION_NOT_FOUND(NOT_FOUND, "알림을 찾을 수 없습니다.", WARN),
    NOTIFICATION_ACCESS_DENIED(FORBIDDEN, "다른 사용자의 알림에 접근할 수 없습니다.", WARN),
    NOTIFICATION_OUTBOX_NOT_FOUND(NOT_FOUND, "알림 outbox 이벤트를 찾을 수 없습니다.", WARN),

    USER_ALREADY_EXISTS(CONFLICT, "이미 존재하는 사용자입니다.", WARN),
    EMAIL_ALREADY_EXISTS(CONFLICT, "이미 사용 중인 이메일입니다.", WARN),
    TAG_ALREADY_EXISTS(CONFLICT, "이미 존재하는 태그입니다.", WARN),
    PASSWORD_LOOKUP_GONE(
            GONE,
            "비밀번호 찾기는 더 이상 지원하지 않습니다. 비밀번호를 재설정하세요.",
            WARN
    ),
    ATTRACTIONS_POST_NOT_ALLOWED(METHOD_NOT_ALLOWED, "GET /api/attractions를 사용하세요.", WARN),
    INVALID_RATING(BAD_REQUEST, "평점은 1 이상 5 이하여야 합니다.", WARN),
    GOOGLE_OAUTH_NOT_CONFIGURED(
            SERVICE_UNAVAILABLE,
            "Google OAuth가 설정되어 있지 않습니다.",
            WARN
    ),

    TOUR_API_CALL_FAILED(BAD_GATEWAY, "Tour API 호출에 실패했습니다.", ERROR),
    EV_CHARGER_API_CALL_FAILED(BAD_GATEWAY, "EV 충전기 API 호출에 실패했습니다.", ERROR),
    DATABASE_DISCONNECTED(SERVICE_UNAVAILABLE, "데이터베이스 연결이 끊어졌습니다.", ERROR),
    INTERNAL_ERROR(INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", ERROR);

    private final ErrorCode code;
    private final String message;
    private final LogLevel logLevel;

    ErrorType(ErrorCode code, String message, LogLevel logLevel) {
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }

    public ErrorCode code() {
        return code;
    }

    public String message() {
        return message;
    }

    public LogLevel logLevel() {
        return logLevel;
    }
}
