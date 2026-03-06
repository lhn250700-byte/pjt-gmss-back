package com.study.spring.Bbs.service;

/**
 * 게시글 임베딩(벡터) 생성 서비스.
 * 텍스트를 벡터(1536차원)로 변환해 유사도 검색에 사용합니다.
 */
public interface EmbeddingService {

    /**
     * 텍스트를 임베딩 벡터로 변환합니다.
     * @param text 임베딩할 텍스트 (제목+본문 등)
     * @return 1536차원 벡터, 또는 API 미설정 시 null
     */
    float[] embed(String text);
}
