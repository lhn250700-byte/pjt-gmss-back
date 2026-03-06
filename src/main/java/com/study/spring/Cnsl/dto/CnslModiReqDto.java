package com.study.spring.Cnsl.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CnslModiReqDto {
	private String cnsler_id;
	private String cnsl_title;
	private String cnsl_content;
	private LocalDate cnsl_date;
	private LocalTime cnsl_start_time;
}
