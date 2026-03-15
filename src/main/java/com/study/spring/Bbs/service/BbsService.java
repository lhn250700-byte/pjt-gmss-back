package com.study.spring.Bbs.service;

import com.study.spring.Bbs.dto.CommentListDto;
import com.study.spring.Bbs.dto.PopularPostClassDto;
import com.study.spring.Bbs.dto.PopularPostDto;
import com.study.spring.Bbs.dto.PostListDto;
import com.study.spring.Bbs.entity.Bbs;
import com.study.spring.Bbs.entity.Bbs_Comment;
import com.study.spring.Bbs.entity.Bbs_Like;
import com.study.spring.Bbs.entity.Cmt_Like;
import com.study.spring.Bbs.repository.BbsCommentRepository;
import com.study.spring.Bbs.repository.CmtLikeRepository;
import com.study.spring.Bbs.repository.BbsLikeRepository;
import com.study.spring.Bbs.repository.BbsRepository;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;
import com.study.spring.keyword.service.KeywordService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Log4j2
public class BbsService {
    /** 인기글 캐시 TTL(초). 이 시간 동안 DB 재조회 없이 캐시 반환 */
    private static final int POPULAR_CACHE_TTL_SEC = 90;
    private final Map<String, CachedPopular> popularCache = new ConcurrentHashMap<>();

    @Autowired
    BbsRepository bbsRepository;
    @Autowired
    BbsCommentRepository bbsCommentRepository;
    @Autowired
    BbsLikeRepository bbsLikeRepository;
    @Autowired
    CmtLikeRepository cmtLikeRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    KeywordService keywordService;
    @Autowired(required = false)
    EmbeddingService embeddingService;

    // [실시간 인기글] — 전용 쿼리 사용(period 바인딩 이슈 방지)
    public List<PopularPostClassDto> findRealtimePopularPosts(String period) {
        List<PopularPostDto> results = bbsRepository.findPopularPostsRealtime();
        return results.stream().map(r -> PopularPostClassDto
                .builder()
                .bbsId(r.getBbsId() != null ? r.getBbsId().longValue() : null)
                .title(r.getTitle())
                .content(r.getContent())
                .views(nullToZero(r.getViews()))
                .commentCount(nullToZero(r.getCommentCount()))
                .bbsLikeCount(nullToZero(r.getBbsLikeCount()))
                .bbsDislikeCount(nullToZero(r.getBbsDisLikeCount()))
                .cmtLikeCount(nullToZero(r.getCmtLikeCount()))
                .cmtDislikeCount(nullToZero(r.getCmtDisLikeCount()))
                .createdAt(r.getCreatedAt())
                .postScore(calculateRealtimeScore(r))
                .build())
                .sorted(Comparator.comparing(PopularPostClassDto::getPostScore).reversed()
                        .thenComparing(PopularPostClassDto::getCreatedAt, Comparator.reverseOrder()))
                .limit(10)
                .toList();
    }
	
	public Double calculateRealtimeScore(PopularPostDto popularPostDto) {
		// score = (조회수 * 1) + (댓글 수 * 3) + ... / (경과시간 + 1)^α — null-safe, 시계 오차 시 NaN 방지
		LocalDateTime createdAt = popularPostDto.getCreatedAt();
		Duration duration = createdAt != null
				? Duration.between(createdAt, LocalDateTime.now())
				: Duration.ZERO;
		double time = Math.max(0, duration.getSeconds() / 3600.0); // 미래 시간이면 0으로 처리해 NaN 방지
		double timeScore = Math.pow(time + 1, 1.2);
		if (timeScore <= 0) return 0.0;
		int views = nullToZero(popularPostDto.getViews());
		int commentCount = nullToZero(popularPostDto.getCommentCount());
		int bbsLike = nullToZero(popularPostDto.getBbsLikeCount());
		int cmtLike = nullToZero(popularPostDto.getCmtLikeCount());
		int bbsDislike = nullToZero(popularPostDto.getBbsDisLikeCount());
		int cmtDislike = nullToZero(popularPostDto.getCmtDisLikeCount());
		return (views + (commentCount * 3) + (bbsLike * 5) + (cmtLike * 1.5)
				- (bbsDislike * 6) - (cmtDislike * 2)) / timeScore;
	}

	private static int nullToZero(Integer v) {
		return v != null ? v : 0;
	}

    // [주간 인기글] — 전용 쿼리 사용
    public List<PopularPostClassDto> findWeeklyPopularPosts(String period) {
        List<PopularPostDto> results = bbsRepository.findPopularPostsWeekly();
        return results.stream().map(r -> PopularPostClassDto
                .builder()
                .bbsId(r.getBbsId() != null ? r.getBbsId().longValue() : null)
                .title(r.getTitle())
                .content(r.getContent())
                .views(nullToZero(r.getViews()))
                .commentCount(nullToZero(r.getCommentCount()))
                .bbsLikeCount(nullToZero(r.getBbsLikeCount()))
                .bbsDislikeCount(nullToZero(r.getBbsDisLikeCount()))
                .cmtLikeCount(nullToZero(r.getCmtLikeCount()))
                .cmtDislikeCount(nullToZero(r.getCmtDisLikeCount()))
                .createdAt(r.getCreatedAt())
                .postScore(calculateWeeklyScore(r))
                .build())
                .sorted(Comparator.comparing(PopularPostClassDto::getPostScore).reversed()
                        .thenComparing(PopularPostClassDto::getCreatedAt, Comparator.reverseOrder()))
                .limit(10)
                .toList();
    }


	public Double calculateWeeklyScore(PopularPostDto popularPostDto) {
		// weekly_score = (주간 조회수 * 1) + (주간 댓글 수 * 2) + ... — null-safe
		int views = nullToZero(popularPostDto.getViews());
		int commentCount = nullToZero(popularPostDto.getCommentCount());
		int bbsLike = nullToZero(popularPostDto.getBbsLikeCount());
		int cmtLike = nullToZero(popularPostDto.getCmtLikeCount());
		int bbsDislike = nullToZero(popularPostDto.getBbsDisLikeCount());
		int cmtDislike = nullToZero(popularPostDto.getCmtDisLikeCount());
		return views + (commentCount * 2) + (bbsLike * 3) + cmtLike
				- (bbsDislike * 4) - (cmtDislike * 1.5);
	}

    // [월간 인기글] — 전용 쿼리 사용
    public List<PopularPostClassDto> findMonthlyPopularPosts(String period) {
        List<PopularPostDto> results = bbsRepository.findPopularPostsMonthly();
        return results.stream().map(r -> PopularPostClassDto
                        .builder()
                        .bbsId(r.getBbsId() != null ? r.getBbsId().longValue() : null)
                        .title(r.getTitle())
                        .content(r.getContent())
                        .views(nullToZero(r.getViews()))
                        .commentCount(nullToZero(r.getCommentCount()))
                        .bbsLikeCount(nullToZero(r.getBbsLikeCount()))
                        .bbsDislikeCount(nullToZero(r.getBbsDisLikeCount()))
                        .cmtLikeCount(nullToZero(r.getCmtLikeCount()))
                        .cmtDislikeCount(nullToZero(r.getCmtDisLikeCount()))
                        .createdAt(r.getCreatedAt())
                        .postScore(calculateWeeklyScore(r))
                        .build())
                .sorted(Comparator.comparing(PopularPostClassDto::getPostScore).reversed()
                        .thenComparing(PopularPostClassDto::getCreatedAt, Comparator.reverseOrder()))
                .limit(10)
                .toList();
    }
		

	// 마이페이지 내 작성 글
	@Transactional(readOnly = true)
	public Page<PostListDto> getPostListByMemberId(String memberId, String keyword, Pageable pageable) {
		return bbsRepository.getPostListByMemberId(memberId, keyword,  pageable);
	}

	// 마이페이지 내 작성 댓글
	@Transactional(readOnly = true)
	public Page<CommentListDto> getCommentListByMemberId(String memberId, Pageable pageable) {
		return bbsRepository.getCommentListByMemberId(memberId, pageable);
	}

    // ========================================
    // 게시글 CRUD 기능
    // ========================================
    
    /**
     * 게시글 작성 (민감 키워드 자동 검사 포함).
     * memberId(헤더 X-User-Id)가 있으면 해당 회원을 작성자로 설정하고, 없으면 anonymous 회원으로 설정 시도.
     */
    @Transactional
    public Bbs createPost(Bbs bbs, String memberIdHeader) {
        String authorId = (memberIdHeader != null && !memberIdHeader.isBlank()) ? memberIdHeader.trim() : "anonymous";
        Member author = memberRepository.findById(authorId).orElse(null);
        if (author == null) {
            log.warn("게시글 작성: 회원 없음(member_id={}). 로그인 또는 X-User-Id에 DB에 존재하는 member_id를 넣어 주세요.", authorId);
            throw new IllegalArgumentException("작성자 회원을 찾을 수 없습니다. 로그인 후 다시 시도해 주세요. (member_id: " + authorId + ")");
        }
        bbs.setMemberId(author);
        // DB bbs.mbti NOT NULL 대응: null이면 빈 문자열로 저장 (자유/공지 등에서 MBTI 미선택 시)
        if (bbs.getMbti() == null) {
            bbs.setMbti("");
        }
        // 글 작성 시 삭제 여부 기본값: null이면 'N'으로 저장
        if (bbs.getDelYn() == null || bbs.getDelYn().isBlank()) {
            bbs.setDelYn("N");
        }
        // 게시글 저장
        Bbs saved = bbsRepository.save(bbs);
        log.info("게시글 작성 완료: bbsId={}", saved.getBbsId());
        
        // 민감 키워드 자동 검사
        String fullContent = (bbs.getTitle() != null ? bbs.getTitle() : "") + " " + 
                            (bbs.getContent() != null ? bbs.getContent() : "");
        
        List<Map<String, Object>> detected = keywordService.detectSensitiveKeywords(fullContent);
        
        // 민감 키워드 발견 시 기록
        if (!detected.isEmpty()) {
            String riskAuthorId = saved.getMemberId() != null ? saved.getMemberId().getMemberId() : null;
            keywordService.recordRiskPost("bbs", saved.getBbs_div(),
                saved.getBbsId().longValue(), fullContent, detected, riskAuthorId);
            log.warn("⚠️ 민감 키워드 감지된 게시글: bbsId={}, 키워드 개수={}",
                saved.getBbsId(), detected.size());
        }

        // 벡터(임베딩) 생성 및 저장 (EmbeddingService 설정 시)
        saveEmbeddingIfAvailable(saved.getBbsId(), fullContent);

        return saved;
    }
    
    /**
     * 게시글 목록 조회 (페이징)
     */
    public Page<Bbs> getPosts(int page, int limit, String bbsDiv, String delYn) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        
        if (bbsDiv != null && !bbsDiv.isEmpty()) {
            return bbsRepository.findByBbsDivAndDelYnOrderByCreatedAtDesc(bbsDiv, delYn, pageable);
        }
        
        return bbsRepository.findByDelYnOrderByCreatedAtDesc(delYn, pageable);
    }
    
    /**
     * 게시글 상세 조회 (member 한 번에 로딩).
     * 없거나 삭제된 글(del_yn='Y')이면 404용 Optional.empty() 반환.
     */
    @Transactional
    public Optional<Bbs> getPostById(Integer bbsId) {
        Optional<Bbs> bbs = bbsRepository.findByIdWithMember(bbsId);
        if (bbs.isEmpty()) {
            return Optional.empty();
        }
        Bbs entity = bbs.get();
        if (entity.getDelYn() != null && "Y".equalsIgnoreCase(entity.getDelYn())) {
            return Optional.empty();
        }
        entity.setViews(entity.getViews() == null ? 1 : entity.getViews() + 1);
        return Optional.of(entity);
    }
    
    /**
     * 게시글 수정
     */
    @Transactional
    public Bbs updatePost(Integer bbsId, Bbs updateData) {
        Bbs bbs = bbsRepository.findById(bbsId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));
        
        if (updateData.getTitle() != null) {
            bbs.setTitle(updateData.getTitle());
        }
        if (updateData.getContent() != null) {
            bbs.setContent(updateData.getContent());
        }
        
        Bbs updated = bbsRepository.save(bbs);
        log.info("게시글 수정 완료: bbsId={}", bbsId);

        String fullContent = (updated.getTitle() != null ? updated.getTitle() : "") + " "
                + (updated.getContent() != null ? updated.getContent() : "");
        saveEmbeddingIfAvailable(updated.getBbsId(), fullContent);

        return updated;
    }
    
    /**
     * 게시글 삭제 (소프트 삭제)
     */
    @Transactional
    public void deletePost(Integer bbsId) {
        Bbs bbs = bbsRepository.findById(bbsId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));
        
        bbs.setDelYn("Y");
        bbsRepository.save(bbs);
        log.info("게시글 삭제 완료: bbsId={}", bbsId);
    }

    // ========================================
    // 댓글
    // ========================================

    public List<Bbs_Comment> getComments(Integer bbsId) {
        return bbsCommentRepository.findByBbsIdAndDelYnOrderByCreatedAtAsc(bbsId);
    }

    /**
     * 댓글 목록 + 좋아요/싫어요 건수 (프론트 연동용)
     */
    public List<Map<String, Object>> getCommentsWithMeta(Integer bbsId) {
        List<Bbs_Comment> list = getComments(bbsId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Bbs_Comment c : list) {
            List<Cmt_Like> likes = cmtLikeRepository.findByCmtId(c);
            long likeCnt = likes.stream().filter(Cmt_Like::isLike).count();
            long dislikeCnt = likes.stream().filter(l -> !l.isLike()).count();
            Map<String, Object> row = new HashMap<>();
            row.put("cmt_id", c.getCmt_id());
            row.put("content", c.getContent());
            row.put("created_at", c.getCreated_at());
            if (c.getMemberId() != null) {
                Map<String, Object> m = new HashMap<>();
                m.put("memberId", c.getMemberId().getMemberId());
                m.put("nickname", c.getMemberId().getNickname());
                row.put("memberId", m);
            }
            row.put("likeCount", likeCnt);
            row.put("dislikeCount", dislikeCnt);
            out.add(row);
        }
        return out;
    }

    @Transactional
    public Bbs_Comment addComment(Integer bbsId, String memberIdStr, String content) {
        Bbs bbs = bbsRepository.findById(bbsId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));
        Member member = memberRepository.findById(memberIdStr)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. 로그인 후 이용해 주세요."));
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해 주세요.");
        }
        Bbs_Comment comment = Bbs_Comment.builder()
                .bbsId(bbs)
                .memberId(member)
                .content(content.trim())
                .delYn("N")
                .build();
        Bbs_Comment saved = bbsCommentRepository.save(comment);
        log.info("댓글 작성: bbsId={}, cmtId={}", bbsId, saved.getCmt_id());

        // 민감 키워드 자동 검사 (댓글)
        try {
            String fullContent = "댓글(cmtId=" + saved.getCmt_id() + "): " + saved.getContent();
            List<Map<String, Object>> detected = keywordService.detectSensitiveKeywords(fullContent);
            if (!detected.isEmpty()) {
                keywordService.recordRiskPost(
                        "bbs_comment",
                        (bbs.getBbs_div() != null && !bbs.getBbs_div().isBlank()) ? bbs.getBbs_div() : "BBS",
                        bbs.getBbsId() != null ? bbs.getBbsId().longValue() : bbsId.longValue(),
                        fullContent,
                        detected,
                        memberIdStr
                );
                log.warn("⚠️ 민감 키워드 감지된 댓글: bbsId={}, cmtId={}, 키워드 개수={}", bbsId, saved.getCmt_id(), detected.size());
            }
        } catch (Exception e) {
            // 댓글 저장은 성공시키되, 감지 기록 실패로 전체 트랜잭션을 깨지 않도록 보호
            log.warn("민감 키워드 댓글 감지/기록 실패: bbsId={}, cmtId={}, err={}", bbsId, saved.getCmt_id(), e.getMessage());
        }

        return saved;
    }

    @Transactional
    public void toggleCommentLike(Integer cmtId, String memberIdStr, boolean isLike) {
        Bbs_Comment cmt = bbsCommentRepository.findById(cmtId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));
        Member member = memberRepository.findById(memberIdStr)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. 로그인 후 이용해 주세요."));
        Optional<Cmt_Like> existing = cmtLikeRepository.findByCmtIdAndMemberIdMemberId(cmt, memberIdStr);
        if (existing.isPresent()) {
            Cmt_Like cl = existing.get();
            cl.setLike(isLike);
            cmtLikeRepository.save(cl);
        } else {
            cmtLikeRepository.save(Cmt_Like.builder()
                    .cmtId(cmt)
                    .memberId(member)
                    .isLike(isLike)
                    .build());
        }
        log.info("댓글 좋아요 토글: cmtId={}, isLike={}", cmtId, isLike);
    }

    public Map<String, Long> getCommentLikeCounts(Integer cmtId) {
        Bbs_Comment cmt = bbsCommentRepository.findById(cmtId).orElse(null);
        if (cmt == null) {
            return Map.of("likeCount", 0L, "dislikeCount", 0L);
        }
        List<Cmt_Like> list = cmtLikeRepository.findByCmtId(cmt);
        long likeCount = list.stream().filter(Cmt_Like::isLike).count();
        long dislikeCount = list.stream().filter(l -> !l.isLike()).count();
        return Map.of("likeCount", likeCount, "dislikeCount", dislikeCount);
    }

    @Transactional
    public void deleteComment(Integer cmtId, String memberIdStr) {
        Bbs_Comment comment = bbsCommentRepository.findById(cmtId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));
        if (!comment.getMemberId().getMemberId().equals(memberIdStr)) {
            throw new IllegalArgumentException("본인 댓글만 삭제할 수 있습니다.");
        }
        comment.setDelYn("Y");
        bbsCommentRepository.save(comment);
        log.info("댓글 삭제: cmtId={}", cmtId);
    }

    // ========================================
    // 좋아요/싫어요
    // ========================================

    @Transactional
    public void toggleLike(Integer bbsId, String memberIdStr, boolean isLike) {
        Bbs bbs = bbsRepository.findById(bbsId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));
        Member member = memberRepository.findById(memberIdStr)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. 로그인 후 이용해 주세요."));
        Optional<Bbs_Like> existing = bbsLikeRepository.findByBbsIdAndMemberId(bbsId, memberIdStr);
        if (existing.isPresent()) {
            Bbs_Like like = existing.get();
            like.setIsLike(isLike);
            bbsLikeRepository.save(like);
        } else {
            Bbs_Like like = Bbs_Like.builder()
                    .bbsId(bbs)
                    .memberId(member)
                    .isLike(isLike)
                    .build();
            bbsLikeRepository.save(like);
        }
        log.info("좋아요 토글: bbsId={}, memberId={}, isLike={}", bbsId, memberIdStr, isLike);
    }

    public Map<String, Long> getLikeCounts(Integer bbsId) {
        List<Bbs_Like> list = bbsLikeRepository.findByBbsId(bbsId);
        long likeCount = list.stream().filter(Bbs_Like::getIsLike).count();
        long dislikeCount = list.stream().filter(l -> !l.getIsLike()).count();
        return Map.of("likeCount", likeCount, "dislikeCount", dislikeCount);
    }

    // ========================================
    // 벡터(임베딩) 유사도 검색
    // ========================================

    /**
     * 검색어와 유사한 게시글 목록 (임베딩 기반). API 키 미설정 시 빈 목록.
     */
    public List<Bbs> findSimilarPosts(String query, int limit) {
        if (embeddingService == null || query == null || query.isBlank()) {
            return List.of();
        }
        float[] vec = embeddingService.embed(query.trim());
        if (vec == null || vec.length == 0) {
            return List.of();
        }
        String vectorStr = toVectorString(vec);
        List<Integer> ids = bbsRepository.findSimilarBbsIds(vectorStr, Math.min(limit, 50));
        if (ids.isEmpty()) {
            return List.of();
        }
        return bbsRepository.findAllById(ids);
    }

    /** 제목+본문으로 임베딩 생성 후 DB에 저장 (설정 시에만) */
    private void saveEmbeddingIfAvailable(Integer bbsId, String text) {
        if (embeddingService == null || text == null || text.isBlank()) {
            return;
        }
        float[] vec = embeddingService.embed(text);
        if (vec != null && vec.length > 0) {
            bbsRepository.updateEmbedding(bbsId, toVectorString(vec));
            log.debug("임베딩 저장 완료: bbsId={}", bbsId);
        }
    }

    private static String toVectorString(float[] arr) {
        if (arr == null || arr.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder().append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(arr[i]);
        }
        return sb.append(']').toString();
    }

    /** 인기글 캐시용 홀더 */
    private static class CachedPopular {
        final List<PopularPostClassDto> list;
        final long cachedAtMs = System.currentTimeMillis();

        CachedPopular(List<PopularPostClassDto> list) {
            this.list = list;
        }

        boolean isValid(int ttlSec) {
            return (System.currentTimeMillis() - cachedAtMs) < (ttlSec * 1000L);
        }
    }

}
