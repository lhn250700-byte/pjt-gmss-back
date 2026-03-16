package com.study.spring.Cnsl.dto;

public interface CounselorListDto {
    String getMemberId();
    String getNickname();
    String getProfile();
    String getText();
    String getImgUrl();

    Integer getCate1Cnt();
    Integer getCate2Cnt();
    Integer getCate3Cnt();

    /** 상담 건수 (cnsl_reg 건수) */
    Integer getCnslCnt();
    /** 리뷰 수 (cnsl_review 건수, 별점 옆 표시용) */
    Integer getReviewCnt();
    Double getAvgEvalPt();

    Integer getCnsl1Price();
    Integer getCnsl2Price();
    Integer getCnsl3Price();
    Integer getCnsl4Price();
    Integer getCnsl5Price();
    Integer getCnsl6Price();
}
