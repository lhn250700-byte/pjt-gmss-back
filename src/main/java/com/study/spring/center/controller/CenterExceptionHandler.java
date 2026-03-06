package com.study.spring.center.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * /api/centers/** 예외 처리 - 500 시 응답 본문에 예외 메시지 포함 (원인 확인용)
 */
@RestControllerAdvice(basePackageClasses = CenterController.class)
@Log4j2
public class CenterExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("Center API error", ex);
        String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", message,
                        "type", ex.getClass().getSimpleName()
                ));
    }
}
