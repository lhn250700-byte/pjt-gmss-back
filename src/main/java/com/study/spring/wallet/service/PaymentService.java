package com.study.spring.wallet.service;

import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;
import com.study.spring.wallet.dto.PaymentPatchRequestDto;
import com.study.spring.wallet.dto.PaymentCreateRequestDto;
import com.study.spring.wallet.entity.Payment;
import com.study.spring.wallet.entity.PointHistory;
import com.study.spring.wallet.entity.Wallet;
import com.study.spring.wallet.repository.PaymentRepository;
import com.study.spring.wallet.repository.PointHistoryRepository;
import com.study.spring.wallet.repository.WalletRepository;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Log4j2
public class PaymentService {
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	PaymentRepository paymentRepository;
	@Autowired
	WalletRepository walletRepository;
	@Autowired
	PointHistoryRepository pointHistoryRepository;
	
	// [결제 승인]
	@Transactional
    public JSONObject confirmPayment(String jsonBody) throws Exception {

        JSONParser parser = new JSONParser();
        String orderId;
        String amount;
        String paymentKey;
        try {
        	// 클라이언트에서 받은 JSON 요청 바디입니다.
            JSONObject requestData = (JSONObject) parser.parse(jsonBody);
            paymentKey = (String) requestData.get("paymentKey");
            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");

            log.info(requestData);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다. 
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.
        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        // 결제를 승인하면 결제수단에서 금액이 차감돼요.
        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes("UTF-8"));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200;

        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        // 결제 성공 및 실패 비즈니스 로직을 구현하세요.
        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(() -> new IllegalArgumentException("페이먼트 정보가 존재하지 않습니다."));
    	Member member = memberRepository.findByEmail(payment.getMemberId().getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));
    	Wallet wallet = walletRepository.findByEmail(payment.getMemberId().getMemberId()).orElseThrow(() -> new IllegalArgumentException("지갑 정보가 존재하지 않습니다."));
    	
    	PointHistory pointHistory = PointHistory
    			.builder()
    			.memberId(member)
    			.amount(Long.parseLong(amount))
    			.pointAfter(wallet.getCurrPoint() + Long.parseLong(amount))
    			.brief("포인트 충전")
    			.build();
    	
    	payment.setStatus("PAID");
    	payment.setHistoryId(pointHistory);
        payment.setImpUid(paymentKey);
    	wallet.setCurrPoint(wallet.getCurrPoint() + Long.parseLong(amount));
        
    	pointHistoryRepository.save(pointHistory);
    	paymentRepository.save(payment);
    	
        responseStream.close();

        return jsonObject;
    }

    // [결제 승인 전]
    @Transactional
	public Long createPayment(PaymentCreateRequestDto requestDto) {   	
		Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));
		
		
		Payment payment = Payment
				.builder()
				.amount(requestDto.getAmount())
				.memberId(member)
				.impUid(requestDto.getOrderId())
				.status("READY")
				.historyId(null)
				.build();
		
		paymentRepository.save(payment);
		return payment.getId();
	}	
    
    // [결제 취소]
    @Transactional
    public void cancelPayment(Long paymentId) {
    	Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new IllegalArgumentException("payment 정보가 존재하지 않습니다."));
    	
    	payment.setStatus("FAILED");
    	
    	paymentRepository.save(payment);
    }
}