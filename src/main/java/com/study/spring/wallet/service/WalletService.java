package com.study.spring.wallet.service;


import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;
import com.study.spring.wallet.entity.Wallet;
import com.study.spring.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WalletService {
	@Autowired
	WalletRepository walletRepository;
	@Autowired
	MemberRepository memberRepository;

	public Long getMyPoint(String email) {
		Wallet wallet = walletRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("지갑 정보가 없습니다."));
		
			
		return wallet.getCurrPoint();
	}
	
}
