package com.study.spring.Bot.controller;

import com.study.spring.Bot.entity.Bot_Msg;
import com.study.spring.Bot.repository.BotMsgRepository;
import com.study.spring.Member.dto.MemberDto;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 플로팅 챗봇 대화 저장용 API.
 * Supabase bot_msg 테이블이 없거나 RLS로 막힐 때, Spring JWT 사용자도 DB(bot_msg)에 저장할 수 있도록 함.
 */
@RestController
public class BotMsgController {

    private final BotMsgRepository botMsgRepository;
    private final MemberRepository memberRepository;

    public BotMsgController(BotMsgRepository botMsgRepository, MemberRepository memberRepository) {
        this.botMsgRepository = botMsgRepository;
        this.memberRepository = memberRepository;
    }

    @PostMapping("/api/bot_msg/session")
    public ResponseEntity<?> upsert(@RequestBody Map<String, Object> body, @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        }
        Member member = memberRepository.findByMemberId(principal.getEmail()).orElse(null);
        if (member == null) {
            return ResponseEntity.status(404).body(Map.of("error", "NOT_FOUND"));
        }

        Integer botId = null;
        if (body != null && body.get("bot_id") != null) {
            String raw = Objects.toString(body.get("bot_id"), "").trim();
            if (!raw.isEmpty()) {
                try {
                    botId = Integer.valueOf(raw);
                } catch (NumberFormatException ignored) {
                    // leave botId null
                }
            }
        }
        boolean endSession = body != null && Boolean.TRUE.equals(body.get("endSession"));
        String summary = body != null ? Objects.toString(body.get("summary"), null) : null;
        Object msgData = body != null ? body.get("msg_data") : null;

        Bot_Msg row;
        if (botId != null) {
            row = botMsgRepository.findByBotIdAndMemberId(botId, member.getMemberId()).orElse(null);
        } else {
            row = null;
        }

        Map<String, Object> msgDataMap = toMsgDataMap(msgData);

        if (row == null) {
            row = Bot_Msg.builder()
                    .memberId(member)
                    .msg_data(msgDataMap)
                    .summary(summary)
                    .build();
        } else {
            if (!msgDataMap.isEmpty()) {
                row.setMsg_data(msgDataMap);
            }
            if (summary != null) {
                row.setSummary(summary);
            }
        }

        row = botMsgRepository.save(row);
        Integer savedBotId = row.getBot_id();
        if (endSession) {
            return ResponseEntity.ok(Map.of("bot_id", savedBotId, "endSession", true));
        }
        return ResponseEntity.ok(Map.of("bot_id", savedBotId));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMsgDataMap(Object msgData) {
        if (msgData == null) return Map.of("content", List.of());
        if (msgData instanceof Map) return (Map<String, Object>) msgData;
        return Map.of("content", msgData instanceof List ? msgData : List.of(msgData));
    }
}
