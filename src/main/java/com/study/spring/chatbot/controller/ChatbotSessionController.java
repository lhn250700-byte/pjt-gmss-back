package com.study.spring.chatbot.controller;

import com.study.spring.Member.dto.MemberDto;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;
import com.study.spring.chatbot.entity.ChatbotSession;
import com.study.spring.chatbot.repository.ChatbotSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
public class ChatbotSessionController {
    private final ChatbotSessionRepository repo;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    public ChatbotSessionController(ChatbotSessionRepository repo, MemberRepository memberRepository, ObjectMapper objectMapper) {
        this.repo = repo;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/api/chatbot/session")
    public ResponseEntity<?> upsert(@RequestBody Map<String, Object> body, @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        Member member = memberRepository.findByMemberId(principal.getEmail()).orElse(null);
        if (member == null) return ResponseEntity.status(404).body(Map.of("error", "NOT_FOUND"));

        String botId = body != null ? Objects.toString(body.get("botId"), "") : "";
        boolean endSession = body != null && Boolean.TRUE.equals(body.get("endSession"));
        String summary = body != null ? Objects.toString(body.get("summary"), null) : null;
        Object msgData = body != null ? body.get("msg_data") : null;
        String msgDataJson = null;
        try {
            if (msgData != null) msgDataJson = objectMapper.writeValueAsString(msgData);
        } catch (Exception e) {
            msgDataJson = Objects.toString(msgData, null);
        }

        ChatbotSession session;
        if (botId != null && !botId.isBlank()) {
            session = repo.findByBotId(botId).orElse(null);
        } else {
            session = null;
        }
        if (session == null) {
            botId = UUID.randomUUID().toString();
            session = ChatbotSession.builder()
                    .botId(botId)
                    .member(member)
                    .build();
        }
        if (msgDataJson != null) session.setMsgData(msgDataJson);
        if (summary != null) session.setSummary(summary);
        session = repo.save(session);

        if (endSession) {
            // 세션 종료는 프론트에서 botId를 비우는 것으로 처리(레코드는 유지)
        }
        return ResponseEntity.ok(Map.of("botId", session.getBotId()));
    }
}

