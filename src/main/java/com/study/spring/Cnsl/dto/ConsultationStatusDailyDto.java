package com.study.spring.Cnsl.dto;

import java.time.LocalDate;

public interface ConsultationStatusDailyDto {
    LocalDate getCnslDt();
    String getCnslerId();
    String getNickname();
    Long getCnslReqCnt();
    Long getCnslDoneCnt();
    Long getCount();
}
