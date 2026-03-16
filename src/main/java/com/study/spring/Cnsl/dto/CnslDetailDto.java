package com.study.spring.Cnsl.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CnslDetailDto {

	private String cnsl_title;
	private String user_nickname;
	private String cnsl_content;
	private String cnsler_name;
	private String cnsler_img_url;
	private String cnsler_text;
	private String cnsler_profile;
	private String cnsl_stat;
	private LocalDateTime created_at;	
}