package com.example.commerce_mvp.presentation.common;

import com.example.commerce_mvp.application.common.dto.ErrorResponse;
import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("Business Exception: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                e.getErrorCode().getStatus().value(),
                e.getErrorCode().getCode(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
    }

    /**
     * 인증 예외 처리
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception e, HttpServletRequest request) {
        log.warn("Authentication Exception: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_TOKEN.getStatus().value(),
                ErrorCode.INVALID_TOKEN.getCode(),
                "인증에 실패했습니다.",
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.INVALID_TOKEN.getStatus()).body(errorResponse);
    }

    /**
     * 접근 거부 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access Denied Exception: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.ACCESS_DENIED.getStatus().value(),
                ErrorCode.ACCESS_DENIED.getCode(),
                ErrorCode.ACCESS_DENIED.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.ACCESS_DENIED.getStatus()).body(errorResponse);
    }

    /**
     * @Valid 검증 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation Exception: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(errorResponse);
    }

    /**
     * @Validated 검증 실패 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("Constraint Violation Exception: {}", e.getMessage());
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(errorResponse);
    }

    /**
     * BindException 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("Bind Exception: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(errorResponse);
    }

    /**
     * 잘못된 HTTP 메서드 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("Method Not Supported Exception: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.METHOD_NOT_ALLOWED.getStatus().value(),
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                ErrorCode.METHOD_NOT_ALLOWED.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus()).body(errorResponse);
    }

    /**
     * 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("Type Mismatch Exception: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                "잘못된 타입의 파라미터입니다.",
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(errorResponse);
    }

    /**
     * 404 Not Found 예외 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("No Handler Found Exception: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                "요청한 리소스를 찾을 수 없습니다.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Illegal Argument Exception: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE.getStatus().value(),
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus()).body(errorResponse);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("Unexpected Exception: ", e);
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value(),
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "서버 내부 오류가 발생했습니다.",
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(errorResponse);
    }
}
