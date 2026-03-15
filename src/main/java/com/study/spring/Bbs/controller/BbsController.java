package com.study.spring.Bbs.controller;

import com.study.spring.Bbs.dto.CommentListDto;
import com.study.spring.Bbs.dto.PopularPostClassDto;
import com.study.spring.Bbs.dto.PostListDto;
import com.study.spring.Bbs.entity.Bbs;
import com.study.spring.Bbs.entity.Bbs_Comment;
import com.study.spring.Bbs.service.BbsService;
import com.study.spring.Member.dto.MemberDto;
import com.study.spring.activity.service.ActivityLogger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "게시판", description = "게시판 CRUD 및 인기글 API")
public class BbsController {
	@Autowired
	BbsService bbsService;

    @Autowired
    ActivityLogger activityLogger;

    // [실시간 인기글]
    @GetMapping("/api/bbs_popularPostRealtimeList")
    public ResponseEntity<?> getRealtimePopularPosts(@RequestParam("period") String period) {
        try {
            return ResponseEntity.ok(bbsService.findRealtimePopularPosts(period));
        } catch (Exception e) {
            log.error("bbs_popularPostRealtimeList error, period={}", period, e);
            return ResponseEntity.ok(List.<PopularPostClassDto>of());
        }
    }

    // [주간 인기글] — 예외 시 500 대신 빈 목록 반환
    @GetMapping("/api/bbs_popularPostWeeklyList")
    public ResponseEntity<?> getWeeklyPopularPosts(@RequestParam("period") String period) {
        try {
            return ResponseEntity.ok(bbsService.findWeeklyPopularPosts(period));
        } catch (Exception e) {
            log.error("bbs_popularPostWeeklyList error, period={}", period, e);
            return ResponseEntity.ok(List.<PopularPostClassDto>of());
        }
    }

    // [월간 인기글] — 예외 시 500 대신 빈 목록 반환
    @GetMapping("/api/bbs_popularPostMonthlyList")
    public ResponseEntity<?> getMonthlyPopularPosts(@RequestParam("period") String period) {
        try {
            return ResponseEntity.ok(bbsService.findMonthlyPopularPosts(period));
        } catch (Exception e) {
            log.error("bbs_popularPostMonthlyList error, period={}", period, e);
            return ResponseEntity.ok(List.<PopularPostClassDto>of());
        }
    }

	// 내 작성 글
	@GetMapping("/api/mypage/postlist")
	public ResponseEntity<Page<PostListDto>> getMyPostList(
			@AuthenticationPrincipal MemberDto member,
			@RequestParam(value ="keyword", required = false) String keyword,
			@PageableDefault(size = 20, sort = "created_at", direction= Sort.Direction.DESC)
			Pageable pageable) {
		
		// 토큰에서 추출한 memberId을 사용
		String memberId = member.getEmail();
		
		return ResponseEntity.ok(
				bbsService.getPostListByMemberId(memberId, keyword, pageable)
			);
	}

	// 내 작성 댓글
	@GetMapping("/api/mypage/commentlist")
	public ResponseEntity<Page<CommentListDto>> getMyCommentList(
			@AuthenticationPrincipal MemberDto member,
			@PageableDefault(size = 20, sort = "created_at", direction= Sort.Direction.DESC)
			Pageable pageable) {
		
		// 토큰에서 추출한 memberId을 사용
		String memberId = member.getEmail();
				
		return ResponseEntity.ok(
				bbsService.getCommentListByMemberId(memberId, pageable)
			);
	}

    
    // ========================================
    // 게시글 CRUD API
    // ========================================
    
    @PostMapping("/api/bbs")
    @Operation(summary = "게시글 작성", description = "새 게시글 작성 (민감 키워드 자동 검사). JWT에서 회원 정보를 읽어 작성자를 설정합니다.")
    public ResponseEntity<?> createPost(
            @RequestBody Bbs bbs,
            @AuthenticationPrincipal MemberDto member,
            HttpServletRequest request) {
        if (bbs.getMbti() == null) {
            bbs.setMbti("");
        }
        if (member == null || member.getEmail() == null || member.getEmail().isBlank()) {
            // GET 목록은 공개지만, 작성은 로그인 사용자만 허용
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }
        try {
            String memberId = member != null ? member.getEmail() : null;
            Bbs saved = bbsService.createPost(bbs, memberId);
            
            // 활동 로그 기록
            String userId = memberId != null ? memberId : "anonymous";
            activityLogger.logActivity(userId, null, "USER", "create", "post", 
                saved.getBbsId().longValue(), "게시글 작성: " + saved.getTitle(), request);
            
            // Lazy memberId 직렬화 방지: 엔티티 대신 필요한 필드만 맵으로 반환
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("bbsId", saved.getBbsId());
            data.put("bbs_div", saved.getBbs_div());
            data.put("title", saved.getTitle());
            data.put("content", saved.getContent());
            data.put("created_at", saved.getCreated_at());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "게시글이 작성되었습니다", "data", data)
            );
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다.";
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "서버 오류가 발생했습니다.";
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }
    
    @GetMapping("/api/bbs")
    @Operation(summary = "게시글 목록", description = "게시글 목록 조회 (페이징, 필터링). 로그인 없이 조회 가능.")
    public ResponseEntity<Page<Bbs>> getPosts(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "bbs_div", required = false) String bbs_div,
            @RequestParam(name = "del_yn", defaultValue = "N") String del_yn) {
        
        return ResponseEntity.ok(bbsService.getPosts(page, limit, bbs_div, del_yn));
    }
    
    @GetMapping("/api/bbs/{id}")
    @Operation(summary = "게시글 상세", description = "특정 게시글 상세 조회")
    public ResponseEntity<?> getPostById(@PathVariable("id") Integer id) {
        return bbsService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/api/bbs/{id}")
    @Operation(summary = "게시글 수정", description = "게시글 내용 수정")
    public ResponseEntity<?> updatePost(
            @PathVariable("id") Integer id,
            @RequestBody Bbs updateData,
            @AuthenticationPrincipal MemberDto member,
            HttpServletRequest request) {
        
        try {
            Bbs updated = bbsService.updatePost(id, updateData);
            
            // 활동 로그 기록
            String userId = member != null ? member.getEmail() : "anonymous";
            activityLogger.logActivity(userId, null, "USER", "update", "post", 
                id.longValue(), "게시글 수정", request);
            
            return ResponseEntity.ok(
                Map.of("message", "게시글이 수정되었습니다", "data", updated)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
    
    @DeleteMapping("/api/bbs/{id}")
    @Operation(summary = "게시글 삭제", description = "게시글 삭제 (소프트 삭제)")
    public ResponseEntity<?> deletePost(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal MemberDto member,
            HttpServletRequest request) {
        
        try {
            bbsService.deletePost(id);
            
            // 활동 로그 기록
            String userId = member != null ? member.getEmail() : "anonymous";
            activityLogger.logActivity(userId, null, "USER", "delete", "post", 
                id.longValue(), "게시글 삭제", request);
            
            return ResponseEntity.ok(
                Map.of("message", "게시글이 삭제되었습니다")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    // ========================================
    // 댓글 API
    // ========================================

    @GetMapping("/api/bbs/{id}/comments")
    @Operation(summary = "댓글 목록", description = "해당 게시글의 댓글 목록 (좋아요/싫어요 건수 포함)")
    public ResponseEntity<?> getComments(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(bbsService.getCommentsWithMeta(id));
    }

    @PostMapping("/api/bbs/{id}/comments")
    @Operation(summary = "댓글 작성", description = "해당 게시글에 댓글 작성")
    public ResponseEntity<?> addComment(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal MemberDto member) {
        try {
            String userId = member != null ? member.getEmail() : "anonymous";
            String content = body != null ? body.get("content") : null;
            Bbs_Comment comment = bbsService.addComment(id, userId, content);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "댓글이 작성되었습니다", "data", comment)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping("/api/bbs/comments/{cmtId}/like")
    @Operation(summary = "댓글 좋아요/싫어요", description = "댓글에 좋아요(true) 또는 싫어요(false). 로그인 필수.")
    public ResponseEntity<?> toggleCommentLike(
            @PathVariable("cmtId") Integer cmtId,
            @RequestBody Map<String, Boolean> body,
            @RequestHeader(value = "X-User-Id", required = false) String memberId) {
        try {
            if (memberId == null || memberId.isBlank() || "anonymous".equalsIgnoreCase(memberId.trim())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "로그인 후 이용해 주세요."));
            }
            boolean isLike = body != null && body.getOrDefault("is_like", true);
            bbsService.toggleCommentLike(cmtId, memberId.trim(), isLike);
            return ResponseEntity.ok(Map.of(
                    "message", "처리되었습니다",
                    "likeCounts", bbsService.getCommentLikeCounts(cmtId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/bbs/comments/{cmtId}")
    @Operation(summary = "댓글 삭제", description = "댓글 삭제 (본인만)")
    public ResponseEntity<?> deleteComment(
            @PathVariable("cmtId") Integer cmtId,
            @AuthenticationPrincipal MemberDto member) {
        try {
            String userId = member != null ? member.getEmail() : "anonymous";
            bbsService.deleteComment(cmtId, userId);
            return ResponseEntity.ok(
                Map.of("message", "댓글이 삭제되었습니다")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    // ========================================
    // 좋아요/싫어요 API
    // ========================================

    @PostMapping("/api/bbs/{id}/like")
    @Operation(summary = "좋아요/싫어요", description = "좋아요(true) 또는 싫어요(false) 토글. 로그인 필수.")
    public ResponseEntity<?> toggleLike(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal MemberDto member) {
        try {
            // 비로그인인 경우 JWT 인증 실패 단계에서 이미 401 처리되지만, 방어적으로 한 번 더 체크
            if (member == null || member.getEmail() == null || member.getEmail().isBlank()) {
                return ResponseEntity.status(401).body(
                    Map.of("error", "로그인 후 이용해 주세요.")
                );
            }
            String userId = member.getEmail();
            boolean isLike = body != null && body.getOrDefault("is_like", true);
            bbsService.toggleLike(id, userId, isLike);
            return ResponseEntity.ok(
                Map.of("message", "처리되었습니다", "likeCounts", bbsService.getLikeCounts(id))
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    @GetMapping("/api/bbs/{id}/like-counts")
    @Operation(summary = "좋아요/싫어요 개수", description = "해당 게시글의 좋아요·싫어요 개수")
    public ResponseEntity<Map<String, Long>> getLikeCounts(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(bbsService.getLikeCounts(id));
    }

    // ========================================
    // 벡터(임베딩) 유사도 검색 API
    // ========================================

    @GetMapping("/api/bbs/search/similar")
    @Operation(summary = "유사 게시글 검색", description = "검색어와 의미적으로 유사한 게시글 목록 (임베딩 기반). OpenAI API 키 설정 시 동작")
    public ResponseEntity<List<Bbs>> getSimilarPosts(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bbsService.findSimilarPosts(query, limit));
    }
}
