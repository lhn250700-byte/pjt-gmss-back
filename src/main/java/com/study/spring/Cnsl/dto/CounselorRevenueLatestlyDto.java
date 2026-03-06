package com.study.spring.Cnsl.dto;

import java.time.LocalDate;

public interface CounselorRevenueLatestlyDto {
    LocalDate getCnslDt();
    String getCnslerId();
    String getNickName();
    String getCnslPriceSum();
    String getCnslPriceCmsn();
    String getCnslExctSum();
    String getExctStat();
    Long getCnslCount();
}
