package com.study.spring.Bbs.dto;

import java.time.LocalDateTime;

public interface PopularPostDto {
    /** DB bbs_id는 INTEGER이므로 네이티브 쿼리 매핑 시 Integer 사용 (Long이면 매핑 예외 발생 가능) */
    Integer getBbsId();
    String getTitle();
    String getContent();
    Integer getViews();
    Integer getCommentCount();
    Integer getBbsLikeCount();
    Integer getBbsDisLikeCount();
    Integer getCmtLikeCount();
    Integer getCmtDisLikeCount();
    LocalDateTime getCreatedAt();
}
