package com.study.spring.keyword.service;

import com.study.spring.keyword.entity.BbsRisk;
import com.study.spring.keyword.entity.SensitiveKeyword;
import com.study.spring.keyword.repository.BbsRiskRepository;
import com.study.spring.keyword.repository.SensitiveKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordService {

    private final SensitiveKeywordRepository keywordRepository;
    private final BbsRiskRepository riskRepository;

    /**
     * 민감 키워드 탐지
     * @param content 검사할 내용
     * @return 탐지된 키워드 리스트
     */
    public List<Map<String, Object>> detectSensitiveKeywords(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptyList();
        }

        List<SensitiveKeyword> keywords = keywordRepository.findByIsActiveTrue();
        List<Map<String, Object>> detected = new ArrayList<>();

        for (SensitiveKeyword kw : keywords) {
            Pattern pattern = Pattern.compile(kw.getKeyword(), Pattern.CASE_INSENSITIVE);
            var matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                Map<String, Object> item = new HashMap<>();
                item.put("keyword", kw.getKeyword());
                item.put("keyword_id", kw.getKeywordId());
                item.put("category", kw.getCategory());
                item.put("severity", kw.getSeverity());
                detected.add(item);
                
                log.warn("⚠️ 민감 키워드 발견: {}", kw.getKeyword());
            }
        }
        
        return detected;
    }

    /**
     * 위험 게시물 기록
     * @param tableId 테이블 구분 (bbs, comment 등)
     * @param bbsDiv 게시물 분류
     * @param bbsId 게시물 ID
     * @param content 원본 내용
     * @param detected 탐지된 키워드 리스트
     */
    @Transactional
    public void recordRiskPost(String tableId, String bbsDiv, Long bbsId, String content,
                               List<Map<String, Object>> detected, String memberId) {
        String truncatedContent = content.length() > 500 ? content.substring(0, 500) + "..." : content;
        
        StringBuilder keywordList = new StringBuilder();
        for (Map<String, Object> kw : detected) {
            if (keywordList.length() > 0) keywordList.append(", ");
            keywordList.append(kw.get("keyword")).append("(심각도:").append(kw.get("severity")).append(")");
        }
        String detectedKeywordsStr = keywordList.toString();
        String detectedInfo = "탐지된 키워드: " + detectedKeywordsStr;
        
        BbsRisk risk = BbsRisk.builder()
                .tableId(tableId)
                .bbsDiv(bbsDiv)
                .bbsId(bbsId)
                .content("원본: " + truncatedContent + "\n\n" + detectedInfo)
                .memberId(memberId)
                .detectedKeywords(detectedKeywordsStr)
                .build();
        
        riskRepository.save(risk);
        log.info("📝 위험 게시물 기록: {} ID {}, 감지키워드: {}", tableId, bbsId, detectedKeywordsStr);
    }

    /**
     * 키워드 목록 조회
     */
    public List<SensitiveKeyword> getKeywords() {
        return keywordRepository.findAll();
    }
    
    /**
     * 활성화된 키워드만 조회
     */
    public List<SensitiveKeyword> getActiveKeywords() {
        return keywordRepository.findByIsActiveTrue();
    }

    /**
     * 키워드 추가
     */
    @Transactional
    public SensitiveKeyword addKeyword(String keyword, String category, int severity) {
        if (severity < 1 || severity > 5) {
            throw new IllegalArgumentException("심각도는 1~5 사이여야 합니다");
        }
        
        SensitiveKeyword kw = SensitiveKeyword.builder()
                .keyword(keyword)
                .category(category)
                .severity(severity)
                .isActive(true)
                .build();
        
        return keywordRepository.save(kw);
    }
    
    /**
     * 키워드가 없을 때만 추가 (초기 데이터 삽입용)
     */
    @Transactional
    public void addKeywordIfAbsent(String keyword, String category, int severity) {
        if (keywordRepository.findByKeyword(keyword).isPresent()) {
            return;
        }
        addKeyword(keyword, category, severity);
    }

    /**
     * 키워드 활성화/비활성화
     */
    @Transactional
    public void toggleKeyword(Long keywordId, boolean isActive) {
        SensitiveKeyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new IllegalArgumentException("키워드를 찾을 수 없습니다"));
        keyword.setIsActive(isActive);
        keywordRepository.save(keyword);
    }
}
