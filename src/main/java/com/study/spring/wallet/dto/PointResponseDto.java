package com.study.spring.wallet.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointResponseDto {
	private String memberId;
	private Long chargedAmount;
	private Long currPoint;
	private String message;
}
