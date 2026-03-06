package com.study.spring.wallet.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;
import com.study.spring.wallet.dto.PaymentDto;
import com.study.spring.wallet.dto.PointHistoryDto;
import com.study.spring.wallet.dto.PointResponseDto;
import com.study.spring.wallet.entity.Payment;
import com.study.spring.wallet.entity.PointHistory;
import com.study.spring.wallet.entity.Wallet;
import com.study.spring.wallet.repository.PaymentRepository;
import com.study.spring.wallet.repository.PointHistoryRepository;
import com.study.spring.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointHistoryService {
	
	@Autowired
	private final PointHistoryRepository pointHistoryRepository;
	private final WalletRepository walletRepository;
	private final PaymentRepository paymentRepository;
	private final MemberRepository memberRepository; 

	@Transactional(readOnly = true)
	public List<PointHistoryDto> getMyPointHistory(String nickname) {
		return pointHistoryRepository.findByMemberId_NicknameOrderByIdDesc(nickname)
				.stream()
				.map(PointHistoryDto::from)
				.toList();
	}

	@Transactional
	public PointResponseDto chargePoint(String memberId, PaymentDto request) {
	    // 1. 회원 및 지갑 조회
	    Member member = memberRepository.findByMemberId(memberId)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
	    
	    Wallet wallet = walletRepository.findByMember(member)
	            .orElseThrow(() -> new IllegalArgumentException("지갑 정보가 없습니다."));
	    
	    // 2. 지갑 잔액 업데이트 (계산을 먼저 해서 pointAfter에 넣을 준비)
	    wallet.addPoint(request.getAmount());
	    
	    // 3. 포인트 이용 내역(PointHistory)을 먼저 생성 및 저장
	    // Payment가 이 객체를 참조해야 하므로 여기서 먼저 'history' 변수를 선언합니다.
	    PointHistory history = PointHistory.builder()
	            .memberId(member)          
	            .amount(request.getAmount())
	            .pointAfter(wallet.getCurrPoint()) 
	            .brief("포인트 충전")        
	            .cnslId(null)              
	            .build();
	    
	    pointHistoryRepository.save(history); // 이 시점에 history 객체에 ID가 부여됨

	    // 4. 생성된 history 객체를 사용하여 결제 정보(Payment) 저장
	    Payment payment = Payment.builder()
	            .impUid(request.getImpUid())
	            .memberId(member)
	            .amount(request.getAmount())
	            .status("PAID")
	            .historyId(history) // 이제 위에서 선언한 history 변수를 인식합니다!
	            .build();
	    
	    paymentRepository.save(payment);
	    
	    // 5. 결과 반환
	    return PointResponseDto.builder()
	            .memberId(memberId)              
	            .chargedAmount(request.getAmount()) 
	            .currPoint(wallet.getCurrPoint())    
	            .message("포인트 충전이 성공적으로 완료되었습니다.")
	            .build();
	}
}
