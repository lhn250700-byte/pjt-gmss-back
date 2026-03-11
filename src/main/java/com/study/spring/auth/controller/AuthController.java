package com.study.spring.auth.controller;

import com.study.spring.Member.dto.MemberDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    @GetMapping("/api/auth/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal MemberDto principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        }
        return ResponseEntity.ok(Map.of(
                "email", principal.getEmail(),
                "nickname", principal.getNickname(),
                "roleNames", principal.getRoleNames()
        ));
    }
}

