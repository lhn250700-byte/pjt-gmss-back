package com.study.spring.keyword.controller;

import com.study.spring.keyword.entity.SensitiveKeyword;
import com.study.spring.keyword.service.KeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
@Tag(name = "민감 키워드", description = "민감 키워드 관리 API")
public class KeywordController {

    private final KeywordService keywordService;

    @GetMapping
    @Operation(summary = "키워드 전체 목록", description = "등록된 모든 민감 키워드 조회")
    public ResponseEntity<List<SensitiveKeyword>> getKeywords() {
        return ResponseEntity.ok(keywordService.getKeywords());
    }

    @GetMapping("/active")
    @Operation(summary = "활성 키워드 목록", description = "활성화된 민감 키워드만 조회")
    public ResponseEntity<List<SensitiveKeyword>> getActiveKeywords() {
        return ResponseEntity.ok(keywordService.getActiveKeywords());
    }

    @PostMapping
    @Operation(summary = "키워드 추가", description = "새로운 민감 키워드 추가")
    public ResponseEntity<?> addKeyword(@RequestBody Map<String, Object> body) {
        try {
            String keyword = (String) body.get("keyword");
            String category = (String) body.get("category");
            int severity = Integer.parseInt(body.get("severity").toString());
            
            SensitiveKeyword kw = keywordService.addKeyword(keyword, category, severity);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "키워드가 추가되었습니다", "data", kw)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "키워드 활성화/비활성화", description = "키워드 사용 여부 토글")
    public ResponseEntity<?> toggleKeyword(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        try {
            boolean isActive = body.getOrDefault("is_active", true);
            keywordService.toggleKeyword(id, isActive);
            return ResponseEntity.ok(
                Map.of("message", "키워드 상태가 변경되었습니다")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
}
