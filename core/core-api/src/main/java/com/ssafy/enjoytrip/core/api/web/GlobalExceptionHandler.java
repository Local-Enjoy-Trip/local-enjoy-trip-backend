package com.ssafy.enjoytrip.core.api.web;

import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorCode;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import com.ssafy.enjoytrip.core.support.error.exception.ClientInputException;
import com.ssafy.enjoytrip.core.support.error.exception.DeprecatedEndpointException;
import com.ssafy.enjoytrip.core.support.error.exception.ExternalServiceException;
import com.ssafy.enjoytrip.core.support.error.exception.InfraUnavailableException;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {
    private static final String INVALID_REQUEST_MESSAGE = "유효하지 않은 요청입니다.";
    private static final String INTERNAL_ERROR_MESSAGE = "서버 내부 오류가 발생했습니다.";

    @ExceptionHandler(CoreException.class)
    ResponseEntity<ApiResponse<Void>> handleCoreException(CoreException exception) {
        ErrorType error = exception.errorType();
        writeWarn(error.code(), error.message());
        return ResponseEntity.status(error.status()).body(ApiResponse.fail(error));
    }

    @ExceptionHandler(ClientInputException.class)
    ResponseEntity<ApiResponse<Void>> handleClientInputException(ClientInputException exception) {
        return warnResponse(HttpStatus.BAD_REQUEST, ErrorCode.C400, exception.getMessage());
    }

    @ExceptionHandler(DeprecatedEndpointException.class)
    ResponseEntity<ApiResponse<Void>> handleDeprecatedEndpointException(DeprecatedEndpointException exception) {
        return warnResponse(HttpStatus.GONE, ErrorCode.C410, exception.getMessage());
    }

    @ExceptionHandler({AuthenticationException.class, AuthenticationCredentialsNotFoundException.class})
    ResponseEntity<ApiResponse<Void>> handleAuthenticationException(Exception exception) {
        return warnResponse(HttpStatus.UNAUTHORIZED, ErrorCode.S401, exception.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class,
            JwtException.class
    })
    ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception) {
        writeWarn(ErrorCode.C400, INVALID_REQUEST_MESSAGE);
        return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.C400, INVALID_REQUEST_MESSAGE));
    }

    @ExceptionHandler(ExternalServiceException.class)
    ResponseEntity<ApiResponse<Void>> handleExternalServiceException(ExternalServiceException exception) {
        return errorResponse(HttpStatus.BAD_GATEWAY, ErrorCode.X502, exception.getMessage(), exception);
    }

    @ExceptionHandler(InfraUnavailableException.class)
    ResponseEntity<ApiResponse<Void>> handleInfraUnavailableException(InfraUnavailableException exception) {
        return errorResponse(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.I503, exception.getMessage(), exception);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.I500, INTERNAL_ERROR_MESSAGE, exception);
    }

    private ResponseEntity<ApiResponse<Void>> warnResponse(HttpStatus status, ErrorCode code, String message) {
        writeWarn(code, message);
        return ResponseEntity.status(status).body(ApiResponse.fail(code, message));
    }

    private ResponseEntity<ApiResponse<Void>> errorResponse(
            HttpStatus status,
            ErrorCode code,
            String message,
            Exception exception
    ) {
        log.error("[{}] {}", code, message, exception);
        return ResponseEntity.status(status).body(ApiResponse.fail(code, message));
    }

    private void writeWarn(ErrorCode code, String message) {
        log.warn("[{}] {}", code, message);
    }
}
