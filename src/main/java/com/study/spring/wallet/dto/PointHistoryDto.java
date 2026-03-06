package com.study.spring.wallet.dto;

import java.time.LocalDateTime;

import com.study.spring.wallet.entity.PointHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointHistoryDto {
	private Long id;
	private Long amount;
	private Long pointAfter;
	private Long cnslId;
	private String brief;
	private LocalDateTime createdAt;

	// 엔티티를 DTO로 변환하는 정적 팩토리 메서드
	public static PointHistoryDto from(PointHistory entity) {
		return PointHistoryDto.builder()
				.id(entity.getId())
				.amount(entity.getAmount())
				.pointAfter(entity.getPointAfter())
				.cnslId(entity.getCnslId())
				.brief(entity.getBrief())
				.createdAt(entity.getCreatedAt())
				.build();
	}
}
