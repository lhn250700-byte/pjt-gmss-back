package com.study.spring.Cnsl.dto;

import java.time.LocalDateTime;

public interface MyCnslListDto {
		String getCnslTitle();
		String getNickname();
		String getCnslStat();
		LocalDateTime getCreatedAt();
}
