package com.study.spring.Cnsl.dto;

public interface CounselorListDto {
    String getMemberId();
    String getNickname();
    String getProfile();
    String getText();

    Integer getCate1Cnt();
    Integer getCate2Cnt();
    Integer getCate3Cnt();

    Integer getCnslCnt();
    Double getAvgEvalPt();

    Integer getCnsl1Price();
    Integer getCnsl2Price();
    Integer getCnsl3Price();
    Integer getCnsl4Price();
    Integer getCnsl5Price();
}
