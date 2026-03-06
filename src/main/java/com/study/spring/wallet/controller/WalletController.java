package com.study.spring.wallet.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.study.spring.wallet.service.WalletService;

@RestController
public class WalletController {
	@Autowired
	WalletService walletService;
	
	// 내 현재 포인트 가져오기
	@GetMapping("/api/wallet_getpoint")
	public Long getMyPoint(@RequestParam("email") String email) {
		return walletService.getMyPoint(email);
	}
}
