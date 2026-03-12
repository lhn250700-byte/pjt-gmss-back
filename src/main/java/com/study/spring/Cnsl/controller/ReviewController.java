package com.study.spring.Cnsl.controller;

import com.study.spring.Cnsl.dto.ReviewDto;
import com.study.spring.Cnsl.entity.Cnsl_Review;
import com.study.spring.Cnsl.service.ReviewService;
import com.study.spring.activity.service.ActivityLogger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "리뷰", description = "상담 리뷰 CRUD API")
public class ReviewController {

    private final ReviewService reviewService;
    private final ActivityLogger activityLogger;

    @PostMapping
    @Operation(summary = "리뷰 작성", description = "상담에 대한 리뷰 작성")
    public ResponseEntity<?> createReview(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal com.study.spring.Member.dto.MemberDto member,
            HttpServletRequest request) {
        
        try {
            String userId = member != null ? member.getEmail() : "anonymous";
            Integer cnslId = Integer.valueOf(body.get("cnsl_id").toString());
            String title = (String) body.get("title");
            String content = (String) body.get("content");
            int evalPt = Integer.parseInt(body.get("eval_pt").toString());
            
            Cnsl_Review review = reviewService.createReview(cnslId, userId, title, content, evalPt);
            
            // 활동 로그 기록
            activityLogger.logActivity(userId, null, "USER", "create", "review", 
                review.getReviewId().longValue(), "리뷰 작성: " + title, request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "리뷰가 작성되었습니다", "data", review)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping
    @Operation(summary = "리뷰 목록", description = "리뷰 목록 조회 (cnsl_id 또는 member_id로 필터링 가능)")
    public ResponseEntity<Page<Cnsl_Review>> getReviews(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "cnsl_id", required = false) Integer cnsl_id,
            @RequestParam(name = "member_id", required = false) String member_id) {
        
        return ResponseEntity.ok(reviewService.getReviews(page, limit, cnsl_id, member_id));
    }

    @GetMapping("/counselor/{memberId}")
    public ResponseEntity<Page<ReviewDto>> getReviewList(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @PathVariable("memberId") String memberId
    ) {
        return ResponseEntity.ok(reviewService.getReviewList(page, size, memberId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "리뷰 상세", description = "특정 리뷰 상세 조회")
    public ResponseEntity<?> getReviewById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(reviewService.getReviewById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "리뷰 수정", description = "리뷰 내용 수정")
    public ResponseEntity<?> updateReview(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal com.study.spring.Member.dto.MemberDto member,
            HttpServletRequest request) {
        
        try {
            String userId = member != null ? member.getEmail() : "anonymous";
            String title = (String) body.get("title");
            String content = (String) body.get("content");
            Integer evalPt = body.get("eval_pt") != null ? Integer.parseInt(body.get("eval_pt").toString()) : null;
            
            Cnsl_Review review = reviewService.updateReview(id, userId, title, content, evalPt);
            
            // 활동 로그 기록
            activityLogger.logActivity(userId, null, "USER", "update", "review", 
                id.longValue(), "리뷰 수정", request);
            
            return ResponseEntity.ok(
                Map.of("message", "리뷰가 수정되었습니다", "data", review)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "리뷰 삭제", description = "리뷰 삭제 (소프트 삭제)")
    public ResponseEntity<?> deleteReview(
            @PathVariable Integer id,
            @AuthenticationPrincipal com.study.spring.Member.dto.MemberDto member,
            HttpServletRequest request) {
        
        try {
            String userId = member != null ? member.getEmail() : "anonymous";
            reviewService.deleteReview(id, userId);
            
            // 활동 로그 기록
            activityLogger.logActivity(userId, null, "USER", "delete", "review", 
                id.longValue(), "리뷰 삭제", request);
            
            return ResponseEntity.ok(
                Map.of("message", "리뷰가 삭제되었습니다")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/counsel/{cnslId}/average")
    @Operation(summary = "평균 평점", description = "특정 상담의 평균 평점 및 통계")
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable Integer cnslId) {
        return ResponseEntity.ok(reviewService.getAverageRating(cnslId));
    }
}
