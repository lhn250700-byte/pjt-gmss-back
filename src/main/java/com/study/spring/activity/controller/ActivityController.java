package com.study.spring.activity.controller;

import com.study.spring.activity.entity.ActivityLog;
import com.study.spring.activity.repository.ActivityLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "활동 내역", description = "사용자 활동 로그 API")
public class ActivityController {

    private final ActivityLogRepository logRepository;

    @GetMapping
    @Operation(summary = "전체 활동 내역", description = "모든 사용자의 활동 내역 조회 (페이징)")
    public ResponseEntity<Page<ActivityLog>> getActivities(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        
        return ResponseEntity.ok(
            logRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, limit))
        );
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "회원별 활동 내역", description = "특정 회원의 활동 내역 조회")
    public ResponseEntity<Page<ActivityLog>> getMemberActivities(
            @PathVariable String memberId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        
        return ResponseEntity.ok(
            logRepository.findByMemberIdOrderByCreatedAtDesc(memberId, PageRequest.of(page - 1, limit))
        );
    }

    @GetMapping("/recent")
    @Operation(summary = "최근 활동", description = "최근 24시간 활동 내역")
    public ResponseEntity<List<ActivityLog>> getRecentActivities() {
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        return ResponseEntity.ok(
            logRepository.findByCreatedAtAfterOrderByCreatedAtDesc(yesterday)
        );
    }

    @GetMapping("/stats")
    @Operation(summary = "활동 통계", description = "기간별 활동 통계 (액션 타입별 집계)")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam(name = "days", defaultValue = "7") int days) {
        
        LocalDateTime start = LocalDateTime.now().minusDays(days);
        List<ActivityLog> logs = logRepository.findByCreatedAtAfterOrderByCreatedAtDesc(start);
        
        // 활동 타입별 집계
        Map<String, Integer> byAction = new HashMap<>();
        Map<String, Integer> byTarget = new HashMap<>();
        
        for (ActivityLog log : logs) {
            byAction.merge(log.getActionType(), 1, Integer::sum);
            byTarget.merge(log.getTargetType(), 1, Integer::sum);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total_activities", logs.size());
        result.put("period_days", days);
        result.put("by_action_type", byAction);
        result.put("by_target_type", byTarget);
        result.put("start_date", start);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/action/{actionType}")
    @Operation(summary = "액션별 활동 조회", description = "특정 액션 타입의 활동 목록 조회")
    public ResponseEntity<List<ActivityLog>> getActivitiesByAction(@PathVariable String actionType) {
        return ResponseEntity.ok(
            logRepository.findByActionTypeOrderByCreatedAtDesc(actionType)
        );
    }
}
