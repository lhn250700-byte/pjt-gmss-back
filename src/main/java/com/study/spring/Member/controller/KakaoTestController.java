package com.study.spring.Member.controller;

import com.study.spring.Member.service.KakaoService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class KakaoTestController {
    @Autowired
    KakaoService kakaoService;

    @GetMapping("/kakao/auth-code") // 카카오에서 설정한 주소(redirect-uri)
    public ResponseEntity<String> kakaoCallback(@RequestParam(required = false) String code,
                                                @RequestParam(required = false) String error, // 인증 실패 시 에러 코드
                                                @RequestParam(required = false) String error_description // 인증 실패 시 에러 메시지
    ) {

        // 인증 실패
        if (error != null) {
            log.info("카카오 로그인 실패 " + error + " / " + error_description);
            return ResponseEntity.badRequest().body("카카오 로그인 실패 " + error_description);
        }

        // 인가 코드 없을 때
        if (code == null || code.isEmpty()) {
            log.info("인가 코드가 없습니다.");
            return ResponseEntity.badRequest().body("인가 코드가 존재하지 않습니다.");
        }

        try {
            kakaoService.kakaoLogin(code);
            return ResponseEntity.ok("카카오 로그인 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 로그인 처리 중 오류 발생 " + e.getMessage());
        }

    }
}
