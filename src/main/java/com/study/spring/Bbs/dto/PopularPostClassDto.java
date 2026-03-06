package com.study.spring.Bbs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class PopularPostClassDto {
    private Long bbsId;
    private String title;
    private String content;
    private Integer views;
    private Integer commentCount;
    private Integer bbsLikeCount;
    private Integer bbsDislikeCount;
    private Integer cmtLikeCount;
    private Integer cmtDislikeCount;
    private LocalDateTime createdAt;
    private Double postScore;
}
