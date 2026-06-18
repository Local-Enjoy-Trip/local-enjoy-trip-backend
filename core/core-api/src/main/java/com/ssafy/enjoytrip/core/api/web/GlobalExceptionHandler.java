package com.ssafy.enjoytrip.core.api.web;

import com.ssafy.enjoytrip.core.support.error.exception.ExternalServiceException;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import com.ssafy.enjoytrip.core.support.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
class GlobalExceptionHandler {

    @ExceptionHandler(CoreException.class)
    ResponseEntity<ApiResponse<Void>> handleCoreException(CoreException exception) {
        ErrorType error = exception.errorType();
        writeLog(error, exception);
        return ResponseEntity.status(resolveHttpStatus(error)).body(ApiResponse.fail(error));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception) {
        ErrorType error = ErrorType.INVALID_REQUEST;
        writeLog(error, exception);
        return ResponseEntity.status(resolveHttpStatus(error)).body(ApiResponse.fail(error));
    }

    @ExceptionHandler(ExternalServiceException.class)
    ResponseEntity<ApiResponse<Void>> handleExternalServiceException(ExternalServiceException exception) {
        ErrorType error = switch (exception.source()) {
            case TOUR_API -> ErrorType.TOUR_API_CALL_FAILED;
            case EV_CHARGER_API -> ErrorType.EV_CHARGER_API_CALL_FAILED;
        };
        writeLog(error, exception);
        return ResponseEntity.status(resolveHttpStatus(error)).body(ApiResponse.fail(error));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        ErrorType error = ErrorType.INTERNAL_ERROR;
        writeLog(error, exception);
        return ResponseEntity.status(resolveHttpStatus(error)).body(ApiResponse.fail(error));
    }

    private HttpStatus resolveHttpStatus(ErrorType error) {
        return switch (error.code()) {
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case GONE -> HttpStatus.GONE;
            case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
            case BAD_GATEWAY -> HttpStatus.BAD_GATEWAY;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private void writeLog(ErrorType error, Exception exception) {
        String message = "[{}] {}";
        switch (error.logLevel()) {
            case TRACE -> log.trace(message, error.code(), error.message(), exception);
            case DEBUG -> log.debug(message, error.code(), error.message(), exception);
            case INFO -> log.info(message, error.code(), error.message());
            case WARN -> log.warn(message, error.code(), error.message());
            case ERROR -> log.error(message, error.code(), error.message(), exception);
            case OFF -> {
            }
        }
    }
}
