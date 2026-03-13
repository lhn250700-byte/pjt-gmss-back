package com.study.spring.Cnsl.controller;

import com.study.spring.Cnsl.entity.Cnsl_Reg;
import com.study.spring.Cnsl.repository.CnslRepository;
import com.study.spring.Member.dto.MemberDto;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
public class CnslChatApiController {

    private final CnslRepository cnslRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public CnslChatApiController(CnslRepository cnslRepository,
                                 MemberRepository memberRepository,
                                 ObjectMapper objectMapper,
                                 SimpMessagingTemplate messagingTemplate) {
        this.cnslRepository = cnslRepository;
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/api/cnsl/{cnslId}")
    public ResponseEntity<?> getCnsl(@PathVariable("cnslId") Long cnslId, @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        Cnsl_Reg cnsl = cnslRepository.findById(cnslId).orElse(null);
        if (cnsl == null) return ResponseEntity.status(404).body(Map.of("error", "NOT_FOUND"));

        return ResponseEntity.ok(Map.of(
                "cnslId", cnsl.getCnslId(),
                "memberId", cnsl.getMemberId() != null ? cnsl.getMemberId().getMemberId() : null,
                "cnslerId", cnsl.getCnslerId() != null ? cnsl.getCnslerId().getMemberId() : null,
                "cnslTp", cnsl.getCnslTp(),
                "cnslStat", cnsl.getCnslStat(),
                "cnslTitle", cnsl.getCnslTitle(),
                "cnslContent", cnsl.getCnslContent(),
                // 프론트는 Date로 파싱하므로 ISO 형태 전달
                "cnslStartTime", cnsl.getCnslStartTime() != null ? LocalDateTime.of(cnsl.getCnslDt() != null ? cnsl.getCnslDt() : LocalDate.now(), cnsl.getCnslStartTime()).toString() : null,
                "cnslEndTime", cnsl.getCnslEndTime() != null ? LocalDateTime.of(cnsl.getCnslDt() != null ? cnsl.getCnslDt() : LocalDate.now(), cnsl.getCnslEndTime()).toString() : null
        ));
    }

    @GetMapping("/api/cnsl/{cnslId}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable("cnslId") Long cnslId, @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        Cnsl_Reg cnsl = cnslRepository.findById(cnslId).orElse(null);
        if (cnsl == null) return ResponseEntity.status(404).body(Map.of("error", "NOT_FOUND"));

        String current = principal.getEmail();
        String memberEmail = cnsl.getMemberId() != null ? cnsl.getMemberId().getMemberId() : null;
        String cnslerEmail = cnsl.getCnslerId() != null ? cnsl.getCnslerId().getMemberId() : null;
        if (memberEmail == null) return ResponseEntity.status(400).body(Map.of("error", "INVALID_COUNSEL"));
        if (!current.equals(memberEmail) && (cnslerEmail == null || !current.equals(cnslerEmail))) {
            return ResponseEntity.status(403).body(Map.of("error", "FORBIDDEN"));
        }
        String otherEmail = current.equals(memberEmail) ? cnslerEmail : memberEmail;
        Member me = memberRepository.findByMemberId(current).orElse(null);
        Member other = otherEmail != null ? memberRepository.findByMemberId(otherEmail).orElse(null) : null;
        return ResponseEntity.ok(Map.of(
                "me", me != null ? toMemberPayload(me) : Map.of("email", current),
                "other", other != null ? toMemberPayload(other) : (otherEmail != null ? Map.of("email", otherEmail) : null),
                "requesterNick", cnsl.getMemberId() != null ? cnsl.getMemberId().getNickname() : memberEmail
        ));
    }

    @GetMapping("/api/cnsl/active")
    public ResponseEntity<?> getActive(@RequestParam("type") String type, @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        String email = principal.getEmail();
        // 단순 조회: 가장 최근 진행중(C) 상담 1건
        // DB 스키마가 고정돼 있어 JPQL 대신 네이티브 쿼리 없이 기본 findAll로 처리(최소 구현)
        List<Cnsl_Reg> all = cnslRepository.findAll();
        Cnsl_Reg match = all.stream()
                .filter(r -> Objects.equals(String.valueOf(r.getCnslTp()), String.valueOf(type)))
                .filter(r -> "C".equalsIgnoreCase(String.valueOf(r.getCnslStat())))
                .filter(r -> (r.getMemberId() != null && email.equals(r.getMemberId().getMemberId())) ||
                             (r.getCnslerId() != null && email.equals(r.getCnslerId().getMemberId())))
                .max(Comparator.comparing(Cnsl_Reg::getCnslId))
                .orElse(null);
        return ResponseEntity.ok(match == null ? Map.of() : Map.of("cnslId", match.getCnslId()));
    }

    @PatchMapping("/api/cnsl/{cnslId}/stat")
    public ResponseEntity<?> updateStat(@PathVariable("cnslId") Long cnslId,
                                        @RequestBody Map<String, Object> body,
                                        @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        Cnsl_Reg cnsl = cnslRepository.findById(cnslId).orElse(null);
        if (cnsl == null) return ResponseEntity.status(404).body(Map.of("error", "NOT_FOUND"));
        String stat = body != null ? Objects.toString(body.get("cnslStat"), "") : "";
        stat = stat.trim().toUpperCase();
        if (stat.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "INVALID_STATUS"));
        cnsl.setCnslStat(stat);
        cnslRepository.save(cnsl);
        // 실시간 상태 브로드캐스트
        messagingTemplate.convertAndSend("/topic/cnsl/" + cnslId + "/stat", (Object) Map.of(
                "cnslId", cnslId,
                "cnslStat", stat
        ));
        return ResponseEntity.ok(Map.of("success", true));
    }

    // /api/cnsl/{cnslId}/chat(GET/POST)는 더 이상 Spring/JPA chat_msg를 사용하지 않고
    // Supabase + testchatpy가 단일 소스로 관리하므로 제거한다.

    @PostMapping("/api/cnsl/{cnslId}/chat/summary-full")
    public ResponseEntity<?> saveSummaryFull(@PathVariable("cnslId") Long cnslId,
                                             @RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        Cnsl_Reg cnsl = cnslRepository.findById(cnslId).orElse(null);
        if (cnsl == null) return ResponseEntity.status(404).body(Map.of("error", "NOT_FOUND"));
        String summary = body != null ? Objects.toString(body.get("summary"), "") : "";
        Object msgData = body != null ? body.get("msg_data") : null;
        // 요약 텍스트는 AI 상담(cnsl_tp = '3') 인 경우에만 cnsl_reg.cnsl_content 에 저장하고,
        // 상세 msg_data(STT 포함)는 Supabase(chat_msg) 쪽에서 관리한다.
        if ("3".equals(String.valueOf(cnsl.getCnslTp())) && !summary.isBlank()) {
            cnsl.setCnslContent(summary);
        }
        cnslRepository.save(cnsl);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/api/cnsl/ai")
    public ResponseEntity<?> createAiCnsl(@RequestBody Map<String, Object> body, @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        Member member = memberRepository.findByMemberId(principal.getEmail()).orElse(null);
        if (member == null) return ResponseEntity.status(404).body(Map.of("error", "NOT_FOUND"));
        String title = body != null ? Objects.toString(body.get("title"), "AI 즉시 상담") : "AI 즉시 상담";
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        Cnsl_Reg saved = cnslRepository.save(Cnsl_Reg.builder()
                .memberId(member)
                .cnslerId(null)
                .cnslTp("3")
                .cnslCate("1")
                .cnslDt(today)
                .cnslStartTime(now)
                .cnslEndTime(now.plusHours(1))
                .cnslStat("C")
                .cnslTitle(title)
                .cnslTodoYn("Y")
                .delYn("N")
                .build());
        return ResponseEntity.ok(Map.of("cnslId", saved.getCnslId()));
    }

    @PostMapping("/api/cnsl/{cnslId}/end")
    public ResponseEntity<?> endCnsl(@PathVariable("cnslId") Long cnslId,
                                    @RequestBody(required = false) Map<String, Object> body,
                                    @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        Cnsl_Reg cnsl = cnslRepository.findById(cnslId).orElse(null);
        if (cnsl == null) return ResponseEntity.status(404).body(Map.of("error", "NOT_FOUND"));
        String cnslContent = body != null ? Objects.toString(body.get("cnslContent"), "") : "";
        if (!cnslContent.isBlank()) cnsl.setCnslContent(cnslContent);
        cnsl.setCnslStat("D");
        cnslRepository.save(cnsl);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private Map<String, Object> toMemberPayload(Member m) {
        return Map.of(
                "email", m.getMemberId(),
                "nickname", m.getNickname(),
                "mbti", m.getMbti(),
                "persona", m.getPersona(),
                "profile", m.getProfile(),
                "role", m.getMemberRoleList() != null && !m.getMemberRoleList().isEmpty() ? m.getMemberRoleList().get(0).name() : null
        );
    }
}

