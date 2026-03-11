package com.study.spring.Member.controller;

import com.study.spring.Member.dto.MemberDto;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MemberProfileController {
    private final MemberRepository memberRepository;

    public MemberProfileController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/api/member/profile")
    public ResponseEntity<?> myProfile(@AuthenticationPrincipal MemberDto principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        }
        Member m = memberRepository.findByMemberId(principal.getEmail()).orElse(null);
        if (m == null) {
            return ResponseEntity.status(404).body(Map.of("error", "NOT_FOUND"));
        }
        return ResponseEntity.ok(Map.of(
                "mbti", m.getMbti(),
                "persona", m.getPersona()
        ));
    }
}

