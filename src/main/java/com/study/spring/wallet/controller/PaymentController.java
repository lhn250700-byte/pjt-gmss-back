package com.study.spring.wallet.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import com.study.spring.wallet.dto.PaymentCreateRequestDto;
import com.study.spring.wallet.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.study.spring.Member.dto.MemberDto;
import com.study.spring.wallet.dto.PaymentDto;
import com.study.spring.wallet.dto.PointHistoryDto;
import com.study.spring.wallet.dto.PointResponseDto;
import com.study.spring.wallet.service.PointHistoryService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@RestController
@Log4j2
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PointHistoryService pointHistoryService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // [결제 승인]
    @PostMapping("/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody) throws Exception {

        logger.info("confirm API called");

        JSONObject result = paymentService.confirmPayment(jsonBody);

        return ResponseEntity.ok(result);
    }
    
    // [결제 승인 전]
    @PostMapping("/api/payments")
    public ResponseEntity<Long> createPayment(@RequestBody PaymentCreateRequestDto requestDto) {
    	return ResponseEntity.status(201).body(paymentService.createPayment(requestDto));
    }
    
    // [결제 중 취소]
    @PatchMapping("/api/payments/{paymentId}")
    public ResponseEntity<Void> cancelPayment(@PathVariable("paymentId") Long paymentId) {
    	paymentService.cancelPayment(paymentId);
    	return ResponseEntity.noContent().build();
    }

    // [환불]
    @PostMapping("/api/payments/cancel/{paymentKey}")
    public ResponseEntity<?> cancelPayment(
        @PathVariable("paymentKey") String paymentKey,
        @RequestBody Map<String, Object> requestBody
    ) throws Exception {
        String cancelReason = (String) requestBody.get("cancelReason");
        Integer cancelAmount = (Integer) requestBody.get("cancelAmount");

        String SECRET_KEY  = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        // Basic 인증 인코딩 (시크릿키 + :)
        String encodedAuth = Base64.getEncoder()
                .encodeToString((SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));

        // 요청 JSON 구성
        String jsonBody;

        if (cancelAmount != null) {
            jsonBody = """
                    {
                      "cancelReason": "%s",
                      "cancelAmount": %d
                    }
                    """.formatted(cancelReason, cancelAmount);
        } else {
            jsonBody = """
                    {
                      "cancelReason": "%s"
                    }
                    """.formatted(cancelReason);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/json")
                // 멱등키 (중복 취소 방지)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        return ResponseEntity
                .status(response.statusCode())
                .body(response.body());
    }
	
	@GetMapping("/api/mypage/points")
	public ResponseEntity<List<PointHistoryDto>> getPointHistory(
			@AuthenticationPrincipal MemberDto member){
		
		List<PointHistoryDto> history = pointHistoryService.getMyPointHistory(member.getNickname());
        System.out.println("아아아아아아아앙" + history);
		
		return ResponseEntity.ok(history);
	}
	
	@PostMapping("/api/mypage/pointcharge")
	public ResponseEntity<PointResponseDto> charge(
			@AuthenticationPrincipal MemberDto member,
			@RequestBody PaymentDto.Request request){
		
		String memberId = member.getEmail();
		
		PointResponseDto response = pointHistoryService.chargePoint(memberId, request);
		
		return ResponseEntity.ok(response);
	}
}
