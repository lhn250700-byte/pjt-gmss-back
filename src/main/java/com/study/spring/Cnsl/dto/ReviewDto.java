package com.study.spring.Cnsl.dto;
import java.time.LocalDateTime;

public interface ReviewDto {
    Long getReviewId();
    String getNickname();
    String getTitle();
    String getContent();
    LocalDateTime getCreatedAt();
    Integer getEvalPt();
}
