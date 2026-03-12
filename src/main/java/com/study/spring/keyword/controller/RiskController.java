package com.study.spring.keyword.controller;

import com.study.spring.keyword.entity.BbsRisk;
import com.study.spring.keyword.repository.BbsRiskRepository;
import com.study.spring.keyword.service.KeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/risks")
@RequiredArgsConstructor
@Tag(name = "위험 게시물", description = "민감 키워드 감지된 게시물 관리 API")
public class RiskController {

    private final BbsRiskRepository riskRepository;
    private final KeywordService keywordService;

    @PostMapping("/check")
    @Operation(summary = "내용 검사", description = "실시간 민감 키워드 검사")
    public ResponseEntity<?> checkContent(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "내용을 입력해주세요")
            );
        }
        
        List<Map<String, Object>> detected = keywordService.detectSensitiveKeywords(content);
        
        int maxSeverity = detected.stream()
                .mapToInt(d -> (Integer) d.get("severity"))
                .max()
                .orElse(0);
        
        return ResponseEntity.ok(Map.of(
            "has_sensitive_keywords", !detected.isEmpty(),
            "detected_keywords", detected,
            "max_severity", maxSeverity,
            "count", detected.size()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "위험 게시물 단건 조회", description = "ID로 위험 게시물 상세 조회 (상담사 게시글 보기용)")
    public ResponseEntity<?> getRiskById(@PathVariable("id") Long id) {
        return riskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "위험 게시물 목록", description = "민감 키워드 감지된 게시물 목록 조회")
    public ResponseEntity<Page<BbsRisk>> getRisks(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        
        return ResponseEntity.ok(
            riskRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, limit))
        );
    }

    @GetMapping("/stats")
    @Operation(summary = "위험 게시물 통계", description = "기간별 위험 게시물 통계")
    public ResponseEntity<?> getStats(@RequestParam(name = "days", defaultValue = "7") int days) {
        LocalDateTime start = LocalDateTime.now().minusDays(days);
        List<BbsRisk> risks = riskRepository.findByCreatedAtAfterOrderByCreatedAtDesc(start);
        
        // 테이블 타입별 집계
        Map<String, Long> byTableType = new HashMap<>();
        for (BbsRisk risk : risks) {
            byTableType.merge(risk.getTableId(), 1L, Long::sum);
        }
        
        return ResponseEntity.ok(Map.of(
            "total_risks", risks.size(),
            "period_days", days,
            "by_table_type", byTableType,
            "start_date", start
        ));
    }

    @GetMapping("/recent")
    @Operation(summary = "최근 위험 게시물", description = "최근 24시간 위험 게시물")
    public ResponseEntity<List<BbsRisk>> getRecentRisks() {
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        return ResponseEntity.ok(
            riskRepository.findByCreatedAtAfterOrderByCreatedAtDesc(yesterday)
        );
    }
}
