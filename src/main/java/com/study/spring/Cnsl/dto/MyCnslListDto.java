package com.study.spring.Cnsl.dto;

import java.time.LocalDateTime;

public interface MyCnslListDto {
		Integer getCnslId();
		String getCnslTp();   // 상담 유형 코드 (3=AI 상담, 그 외=상담사 상담)
		String getCnslType();
		String getCnslTitle();
		String getNickname();
		String getCnslStat();
		LocalDateTime getCreatedAt();
}
