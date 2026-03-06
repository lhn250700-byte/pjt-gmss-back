package com.study.spring.Cnsl.dto;

import java.time.LocalDate;

public interface CounselDetailDto {
    LocalDate getCnslDt();
    String getCnslTitle();
    String getCnslContent();
    String getCnslStatNm();
    String getNickname();
    String getMbti();
    String getGender();
    String getAge();
    String getText();
}
