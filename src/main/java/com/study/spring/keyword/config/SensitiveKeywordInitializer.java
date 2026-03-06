package com.study.spring.keyword.config;

import com.study.spring.keyword.service.KeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 기동 시 상담 관련 위험 단어(민감 키워드)를 DB에 등록합니다.
 * 자살·자해 등 변형 표현 포함. 이미 존재하는 키워드는 건너뜁니다.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class SensitiveKeywordInitializer implements ApplicationRunner {

    private final KeywordService keywordService;

    /** 상담·위기 관련 위험 단어 목록 (키워드, 카테고리, 심각도 1~5) */
    private static final Object[][] DEFAULT_KEYWORDS = {
        // 자살 관련 (심각도 5)
        { "자살", "상담위험", 5 },
        { "자@살", "상담위험", 5 },
        { "자\\.살", "상담위험", 5 },
        { "자 살", "상담위험", 5 },
        { "제목.*자살", "상담위험", 5 },
        { "죽고\\s*싶다", "상담위험", 5 },
        { "죽고\\s*싶어", "상담위험", 5 },
        { "목숨\\s*끊", "상담위험", 5 },
        { "생을\\s*마감", "상담위험", 5 },
        { "스스로\\s*목숨", "상담위험", 5 },
        // 자해·폭력 (심각도 4~5)
        { "자해", "상담위험", 5 },
        { "손목\\s*그어", "상담위험", 5 },
        { "살인", "상담위험", 5 },
        { "동반\\s*자살", "상담위험", 5 },
        { "귀신\\s*되", "상담위험", 4 },
        { "사망\\s*희망", "상담위험", 5 },
        // 위기 표현 (심각도 3~4)
        { "너무\\s*힘들어\\s*죽", "상담위험", 4 },
        { "차라리\\s*죽", "상담위험", 5 },
        { "더\\s*이상\\s*못\\s*살", "상담위험", 4 },
        { "삶이\\s*의미없", "상담위험", 4 },
    };

    @Override
    public void run(ApplicationArguments args) {
        try {
            for (Object[] row : DEFAULT_KEYWORDS) {
                String keyword = (String) row[0];
                String category = (String) row[1];
                int severity = (Integer) row[2];
                keywordService.addKeywordIfAbsent(keyword, category, severity);
            }
            log.info("민감 키워드 초기 데이터 등록 완료 (상담 위험 단어)");
        } catch (Exception e) {
            log.warn("민감 키워드 초기 데이터 등록 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
        }
    }
}
