package com.study.spring.Cnsl.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface CnslerDateDto {
    LocalDate getCnslDt();
    LocalTime getCnslStartTime();
    String getNickname();
}