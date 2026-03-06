package com.study.spring.Cnsl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class CnslDatePerMonthClassDto {
    private LocalDate monthStart;
    private LocalDate monthEnd;
    private Integer totalCnt;
    private Integer reservedCnt;
    private Integer completedCnt;
}
