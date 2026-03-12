package com.study.spring.ai.controller;

import com.study.spring.Member.dto.MemberDto;
import com.study.spring.ai.config.TestChatPyProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class TestChatPyProxyController {
    private final RestClient restClient;

    public TestChatPyProxyController(TestChatPyProperties props) {
        String base = (props.baseUrl() == null ? "" : props.baseUrl().trim()).replaceAll("/$", "");
        this.restClient = RestClient.builder().baseUrl(base).build();
    }

    @PostMapping("/api/summarize")
    public ResponseEntity<?> summarize(@RequestPart("msg_data") String msgData,
                                       @RequestPart(value = "audio_user", required = false) MultipartFile audioUser,
                                       @RequestPart(value = "audio_cnsler", required = false) MultipartFile audioCnsler,
                                       @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("msg_data", msgData);
        if (audioUser != null && !audioUser.isEmpty()) form.add("audio_user", audioUser.getResource());
        if (audioCnsler != null && !audioCnsler.isEmpty()) form.add("audio_cnsler", audioCnsler.getResource());

        Object data = restClient.post()
                .uri("/api/summarize")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form)
                .retrieve()
                .body(Object.class);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/api/testchatpy/chat")
    public ResponseEntity<?> siteChat(@RequestBody Map<String, Object> body,
                                      @AuthenticationPrincipal MemberDto principal) {
        // 미로그인도 챗봇 이용 가능. 사용자 식별은 JWT/세션만 사용, 커스텀 헤더 없음
        Object data = restClient.post()
                .uri("/api/site-chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/ai/chat/{cnslId}")
    public ResponseEntity<?> getAiChat(@PathVariable("cnslId") Long cnslId,
                                       @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        Object data = restClient.get()
                // 커스텀 헤더(X-User-Email) 없이 query로 member_id 전달
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ai/chat/{id}")
                        // username = Spring Security User의 username(=email)
                        .queryParam("member_id", principal.getUsername())
                        .build(cnslId))
                .retrieve()
                .body(Object.class);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/api/ai/chat/{cnslId}")
    public ResponseEntity<?> postAiChat(@PathVariable("cnslId") Long cnslId,
                                        @RequestBody Map<String, Object> body,
                                        @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        Object data = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ai/chat/{id}")
                        .queryParam("member_id", principal.getUsername())
                        .build(cnslId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Object.class);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/api/ai/chat/{cnslId}/summary")
    public ResponseEntity<?> aiSummary(@PathVariable("cnslId") Long cnslId,
                                       @AuthenticationPrincipal MemberDto principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "UNAUTHORIZED"));
        Object data = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ai/chat/{id}/summary")
                        .queryParam("member_id", principal.getUsername())
                        .build(cnslId))
                .retrieve()
                .body(Object.class);
        return ResponseEntity.ok(data);
    }
}

