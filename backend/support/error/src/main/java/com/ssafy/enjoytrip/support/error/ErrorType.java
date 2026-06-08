package com.ssafy.enjoytrip.support.error;

import static com.ssafy.enjoytrip.support.error.ErrorCode.BAD_GATEWAY;
import static com.ssafy.enjoytrip.support.error.ErrorCode.BAD_REQUEST;
import static com.ssafy.enjoytrip.support.error.ErrorCode.CONFLICT;
import static com.ssafy.enjoytrip.support.error.ErrorCode.FORBIDDEN;
import static com.ssafy.enjoytrip.support.error.ErrorCode.GONE;
import static com.ssafy.enjoytrip.support.error.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.ssafy.enjoytrip.support.error.ErrorCode.METHOD_NOT_ALLOWED;
import static com.ssafy.enjoytrip.support.error.ErrorCode.NOT_FOUND;
import static com.ssafy.enjoytrip.support.error.ErrorCode.SERVICE_UNAVAILABLE;
import static com.ssafy.enjoytrip.support.error.ErrorCode.UNAUTHORIZED;
import static com.ssafy.enjoytrip.support.error.LogLevel.ERROR;
import static com.ssafy.enjoytrip.support.error.LogLevel.WARN;

public enum ErrorType {
    INVALID_ACTION(BAD_REQUEST, "Invalid action", WARN),
    MISSING_REQUIRED_FIELDS(BAD_REQUEST, "Missing required fields", WARN),
    MISSING_ID(BAD_REQUEST, "Missing id", WARN),
    MISSING_USER_ID(BAD_REQUEST, "Missing userId", WARN),
    INVALID_REQUEST(BAD_REQUEST, "Invalid request", WARN),
    INVALID_ID(BAD_REQUEST, "Invalid id", WARN),
    INVALID_POINTS(BAD_REQUEST, "Invalid points", WARN),
    INVALID_LATITUDE_OR_LONGITUDE(BAD_REQUEST, "Invalid latitude or longitude", WARN),
    INVALID_USER_ID(BAD_REQUEST, "User ID must be 4-20 letters, numbers, or underscores", WARN),
    INVALID_NAME(BAD_REQUEST, "Name must be 2-30 characters", WARN),
    INVALID_EMAIL(BAD_REQUEST, "Invalid email", WARN),
    INVALID_PASSWORD(BAD_REQUEST, "Password must be 8-64 characters and include letters and numbers", WARN),

    AUTHENTICATION_REQUIRED(UNAUTHORIZED, "Authentication required", WARN),
    INVALID_CREDENTIALS(UNAUTHORIZED, "Invalid credentials", WARN),
    ACCESS_DENIED(FORBIDDEN, "Cannot access another user's account", WARN),

    USER_NOT_FOUND(NOT_FOUND, "User not found", WARN),
    ATTRACTION_NOT_FOUND(NOT_FOUND, "Attraction not found", WARN),
    TAG_NOT_FOUND(NOT_FOUND, "Tag not found", WARN),
    POST_NOT_FOUND(NOT_FOUND, "Post not found", WARN),
    HOTPLACE_NOT_FOUND(NOT_FOUND, "Hotplace not found", WARN),
    PLAN_NOT_FOUND(NOT_FOUND, "Plan not found", WARN),
    NOTICE_NOT_FOUND(NOT_FOUND, "Notice not found", WARN),

    USER_ALREADY_EXISTS(CONFLICT, "User already exists", WARN),
    EMAIL_ALREADY_EXISTS(CONFLICT, "Email already exists", WARN),
    TAG_ALREADY_EXISTS(CONFLICT, "Tag already exists", WARN),
    PASSWORD_LOOKUP_GONE(GONE, "Password lookup is no longer supported. Please reset your password.", WARN),
    ATTRACTIONS_POST_NOT_ALLOWED(METHOD_NOT_ALLOWED, "Use GET /api/attractions", WARN),
    INVALID_RATING(BAD_REQUEST, "Rating must be between 1 and 5", WARN),
    GOOGLE_OAUTH_NOT_CONFIGURED(SERVICE_UNAVAILABLE, "Google OAuth is not configured", WARN),

    TOUR_API_CALL_FAILED(BAD_GATEWAY, "Tour API call failed", ERROR),
    EV_CHARGER_API_CALL_FAILED(BAD_GATEWAY, "EV charger API call failed", ERROR),
    DATABASE_DISCONNECTED(SERVICE_UNAVAILABLE, "Database disconnected", ERROR),
    INTERNAL_ERROR(INTERNAL_SERVER_ERROR, "Internal server error", ERROR);

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
