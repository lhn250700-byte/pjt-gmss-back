package com.study.spring.Cnsl.dto;

import java.time.LocalDate;

public interface CnslDatePerMonthDto {
    LocalDate getMonthStart();
    Integer getTotalCnt();
    Integer getReservedCnt();
    Integer getCompletedCnt();
}
