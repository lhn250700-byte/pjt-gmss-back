package com.study.spring.Bbs.dto;

import java.time.LocalDateTime;

public interface PopularPostDto {
    Long getBbsId();
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
