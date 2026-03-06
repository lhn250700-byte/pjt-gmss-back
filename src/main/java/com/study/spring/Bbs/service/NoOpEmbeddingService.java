package com.study.spring.Bbs.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * API 키 미설정 시 사용하는 임베딩 서비스. 항상 null 반환.
 */
@Service
@ConditionalOnMissingBean(EmbeddingService.class)
public class NoOpEmbeddingService implements EmbeddingService {

    @Override
    public float[] embed(String text) {
        return null;
    }
}
