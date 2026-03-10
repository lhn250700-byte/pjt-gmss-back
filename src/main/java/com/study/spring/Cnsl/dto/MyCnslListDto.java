package com.study.spring.Cnsl.dto;

import java.time.LocalDateTime;

public interface MyCnslListDto {
		Integer getCnslId();
		String getCnslType();
		String getCnslTitle();
		String getNickname();
		String getCnslStat();
		LocalDateTime getCreatedAt();
}
