package com.study.spring.Cnsl.service;

import com.study.spring.Cnsl.dto.ReviewDto;
import com.study.spring.Cnsl.entity.Cnsl_Reg;
import com.study.spring.Cnsl.entity.Cnsl_Review;
import com.study.spring.Cnsl.repository.CnslRepository;
import com.study.spring.Cnsl.repository.CnslReviewRepository;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;
import com.study.spring.keyword.service.KeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final CnslReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final CnslRepository cnslRepository;
    private final KeywordService keywordService;

    /**
     * 리뷰 작성
     */
    @Transactional
    public Cnsl_Review createReview(Integer cnslId, String memberId, String title, String content, int evalPt) {
        // 평점 검증
        if (evalPt < 1 || evalPt > 5) {
            throw new IllegalArgumentException("평점은 1~5 사이여야 합니다");
        }
        
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));
        
        // 상담 조회 (Integer -> Long 변환)
        Cnsl_Reg cnsl = cnslRepository.findById(cnslId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("상담을 찾을 수 없습니다"));
        
        Cnsl_Review review = Cnsl_Review.builder()
                .memberId(member)
                .cnslId(cnsl)
                .title(title)
                .content(content)
                .evalPt(evalPt)
                .delYn("N")
                .build();
        
        Cnsl_Review saved = reviewRepository.save(review);
        log.info("리뷰 작성 완료: reviewId={}, cnslId={}, memberId={}", saved.getReviewId(), cnslId, memberId);

        // 민감 키워드 자동 검사 (상담 내역: 리뷰)
        try {
            String fullContent = (title != null ? title : "") + " " + (content != null ? content : "");
            List<Map<String, Object>> detected = keywordService.detectSensitiveKeywords(fullContent);
            if (!detected.isEmpty()) {
                keywordService.recordRiskPost(
                        "cnsl_review",
                        "CNSL",
                        cnsl.getCnslId(),
                        fullContent,
                        detected,
                        memberId
                );
                log.warn("⚠️ 민감 키워드 감지된 리뷰: cnslId={}, reviewId={}, 키워드 개수={}",
                        cnsl.getCnslId(), saved.getReviewId(), detected.size());
            }
        } catch (Exception e) {
            log.warn("민감 키워드 리뷰 감지/기록 실패: cnslId={}, reviewId={}, err={}",
                    cnsl.getCnslId(), saved.getReviewId(), e.getMessage());
        }
        
        return saved;
    }

    /**
     * 리뷰 목록 조회
     */
    public Page<Cnsl_Review> getReviews(int page, int limit, Integer cnslId, String memberId) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        
        // 특정 상담의 리뷰만
        if (cnslId != null) {
            return reviewRepository.findByCnslIdAndDelYn(cnslId, "N", pageable);
        }
        
        // 특정 회원의 리뷰만
        if (memberId != null) {
            return reviewRepository.findByMemberIdAndDelYn(memberId, "N", pageable);
        }
        
        // 전체 리뷰
        return reviewRepository.findByDelYnOrderByCreatedAtDesc("N", pageable);
    }

    /**
     * 리뷰 상세 조회
     */
    public Cnsl_Review getReviewById(Integer id) {
        Cnsl_Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다"));
        
        if ("Y".equals(review.getDelYn())) {
            throw new IllegalArgumentException("삭제된 리뷰입니다");
        }
        
        return review;
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public Cnsl_Review updateReview(Integer id, String memberId, String title, String content, Integer evalPt) {
        Cnsl_Review review = getReviewById(id);
        
        // 권한 확인
        if (!review.getMemberId().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다");
        }
        
        // 수정
        if (title != null && !title.isBlank()) {
            review.setTitle(title);
        }
        if (content != null && !content.isBlank()) {
            review.setContent(content);
        }
        if (evalPt != null && evalPt >= 1 && evalPt <= 5) {
            review.setEvalPt(evalPt);
        }
        
        Cnsl_Review updated = reviewRepository.save(review);
        log.info("리뷰 수정 완료: reviewId={}", id);
        
        return updated;
    }

    /**
     * 리뷰 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteReview(Integer id, String memberId) {
        Cnsl_Review review = getReviewById(id);
        
        // 권한 확인
        if (!review.getMemberId().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다");
        }
        
        // 소프트 삭제
        review.setDelYn("Y");
        reviewRepository.save(review);
        
        log.info("리뷰 삭제 완료: reviewId={}", id);
    }

    /**
     * 상담별 평균 평점 및 통계
     */
    public Map<String, Object> getAverageRating(Integer cnslId) {
        List<Cnsl_Review> reviews = reviewRepository.findAllByCnslIdAndDelYn(cnslId, "N");
        
        if (reviews.isEmpty()) {
            return Map.of(
                "average_rating", 0.0,
                "total_reviews", 0,
                "rating_distribution", Map.of()
            );
        }
        
        // 평균 계산
        double avg = reviews.stream()
                .mapToInt(Cnsl_Review::getEvalPt)
                .average()
                .orElse(0);
        
        // 평점 분포 계산 (1점~5점)
        Map<Integer, Long> distribution = new java.util.HashMap<>();
        for (int i = 1; i <= 5; i++) {
            int rating = i;
            long count = reviews.stream()
                    .filter(r -> r.getEvalPt() == rating)
                    .count();
            distribution.put(rating, count);
        }
        
        return Map.of(
            "average_rating", Math.round(avg * 10) / 10.0,
            "total_reviews", reviews.size(),
            "rating_distribution", distribution
        );
    }

    // [특정 상담사의 리뷰 리스트]
    public Page<ReviewDto> getReviewList(int page, int size, String memberId) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.getReviewList(pageable, memberId);
    }
}
