package com.study.spring.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreateRequestDto {
	private String email;
	private Long amount;
	private String orderId;
}
