package com.study.spring.wallet.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

public interface PaymentDto {
	Long getId();
	String getImpUid();
	Long getMemberId();
	Long getAmount();
	String getStatus();
	Long getHistoryId();
	LocalDateTime getCreatedAt();
	
	@Getter
    @NoArgsConstructor
    public static class Request implements PaymentDto {
        private Long id;
        private String impUid;
        private Long memberId;
        private Long amount;
        private String status;
        private Long historyId;
        private LocalDateTime createdAt;
    }
}
