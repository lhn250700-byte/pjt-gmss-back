package com.study.spring.Cnsl.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.lang.String;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CnslReqDto {
	private String cnsl_cate;
	private String cnsl_tp;
	private String member_id;
	private String cnsler_id;
	private String cnsl_title;
	private String cnsl_content;
	private LocalDate cnsl_date;
	private LocalTime cnsl_start_time;
}
