package com.study.spring.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * API 전역 예외 처리.
 * - 요청 바디 역직렬화 실패 등 컨트롤러 진입 전 예외도 처리하여 500 대신 메시지 반환.
 */
@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("요청 바디 역직렬화 실패: {}", e.getMessage());
        Throwable cause = e.getCause();
        String msg = "요청 형식이 올바르지 않습니다.";
        if (cause instanceof JsonMappingException) {
            msg = "JSON 형식 오류: " + (cause.getMessage() != null ? cause.getMessage() : msg);
        } else if (cause != null && cause.getMessage() != null) {
            msg = cause.getMessage();
        }
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("API 예외 발생", e);
        String msg = e.getMessage() != null ? e.getMessage() : "서버 오류가 발생했습니다.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
    }
}
