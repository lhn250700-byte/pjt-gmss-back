package com.study.spring.Cnsl.dto;

import java.time.LocalTime;

public interface CategoryRevenueStatisticsDto {
    String getCode();
    String getCodeName();
    String getCnslPriceSum();
    String getCnslPriceCmsn();
    String getCnslExctSum();
    Long getCnslCount();
    String getAvgCnslTime();
}
