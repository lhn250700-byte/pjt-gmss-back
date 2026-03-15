package com.study.spring.Member.controller;

import com.study.spring.Member.dto.*;
import com.study.spring.Member.service.MemberService;
import com.study.spring.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import com.study.spring.Member.entity.Member;

@RestController
@Log4j2
public class MemberController {
	@Autowired
	MemberService memberService;
	@Autowired
	AuthenticationManager authenticationManager;

	@GetMapping("/")
	public String hello() {
		return "hello";
	}

	/** REST 로그인: JSON 수신 후 인증, JWT·쿠키 반환 (form login 대체용) */
	@PostMapping("/api/auth/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
		String username = loginRequest != null ? loginRequest.getUsername() : null;
		String password = loginRequest != null ? loginRequest.getPassword() : null;
		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "LOGIN_FAILED", "message", "이메일과 비밀번호를 입력해주세요."));
		}
		try {
			Authentication auth = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(username.trim(), password));
			Object principal = auth.getPrincipal();
			if (!(principal instanceof MemberDto memberDto)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "LOGIN_FAILED", "message", "인증 정보를 처리할 수 없습니다."));
			}
			Map<String, Object> claims = memberDto.getClaims();
			String accessToken = JWTUtil.generateToken(claims, 10);
			String refreshToken = JWTUtil.generateToken(claims, 60 * 24);
			// accessToken은 응답 body만 사용, 쿠키에는 refreshToken만 저장
			Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
			refreshTokenCookie.setHttpOnly(true);
			refreshTokenCookie.setPath("/");
			refreshTokenCookie.setMaxAge(60 * 60 * 24);
			refreshTokenCookie.setAttribute("SameSite", "None");
			refreshTokenCookie.setSecure(true);
			response.addCookie(refreshTokenCookie);
			Map<String, Object> body = new HashMap<>(claims);
			body.put("accessToken", accessToken);
			log.info("REST 로그인 성공: {}", memberDto.getEmail());
			return ResponseEntity.ok().body(body);
		} catch (AuthenticationException e) {
			log.warn("로그인 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "LOGIN_FAILED", "message", "이메일 또는 비밀번호를 확인해주세요."));
		}
	}

	@PostMapping("/api/member/signup")
	public ResponseEntity<?> register(@RequestBody SignUpDto signUpDto) {
		memberService.register(signUpDto);
		return ResponseEntity.ok("회원가입 성공");
	}

	@PatchMapping("/api/member/kakao-signup")
	public ResponseEntity<?> completeKakaoSignup(@AuthenticationPrincipal MemberDto principal,
			@RequestBody KakaoSignUpDto kakaoSignUpDto) {
		if (principal == null) {
			throw new IllegalStateException("인증되지 않은 사용자입니다.");
		}

		memberService.kakaoRegister(principal.getEmail(), kakaoSignUpDto);

		return ResponseEntity.ok("회원가입 성공");
	}

	@GetMapping("/api/user/info")
	public Map<String, Object> getUserInfo(@AuthenticationPrincipal MemberDto principal,
			Authentication authentication) {

		if (principal == null) {
			return Map.of("authentication", false, "message", "인증되지 않은 사용자입니다.");
		}

		return Map.of("authentication", true, "username", principal.getEmail(), "authorities",
				authentication.getAuthorities(), "message", "jwt인증 통과 완료"

		);
	}

	@PostMapping("/api/auth/refresh")
	public ResponseEntity<Map<String, Object>> refreshToken(
			@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {

		try {
			// 1) refreshToken 쿠키 확인
			if (refreshToken == null || refreshToken.isEmpty()) {
				log.warn("refreshToken 쿠키가 없습니다.");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "refreshToken이 없습니다."));
			}

			// 2) refreshToken 검증
			Map<String, Object> claims;
			try {
				claims = JWTUtil.validateToken(refreshToken);
			} catch (Exception e) {
				log.warn("refreshToken 검증 실패: {}", e.getMessage());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "유효하지 않은 refreshToken입니다."));
			}

			String email = (String) claims.get("email");
			MemberDto member = memberService.getMemberByEmail(email);
			if (member == null) {
				log.warn("DB에 해당 사용자가 존재하지 않습니다. email={}", email);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("error", "존재하지 않는 사용자입니다."));
			}

			Map<String, Object> newClaims = member.getClaims();


			// 3) 새로운 accessToken 생성
			String newAccessToken = JWTUtil.generateToken(newClaims, 10); // 10분

			// 4) refreshToken 회전 정책: 새로운 refreshToken 생성 및 쿠키 설정
			String newRefreshToken = JWTUtil.generateToken(newClaims, 60 * 24); // 24시간

			Cookie refreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
			refreshTokenCookie.setHttpOnly(true);
			refreshTokenCookie.setPath("/");
			refreshTokenCookie.setMaxAge(60 * 60 * 24); // 24시간
			refreshTokenCookie.setAttribute("SameSite", "None");
			refreshTokenCookie.setSecure(true);
			response.addCookie(refreshTokenCookie);
			// accessToken은 쿠키에 넣지 않음, 응답 body만 사용

			// 5) 응답 반환
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("accessToken", newAccessToken);
			responseBody.put("email", newClaims.get("email"));
			responseBody.put("nickname", newClaims.get("nickname"));
			responseBody.put("social", newClaims.get("social"));
			responseBody.put("roleNames", newClaims.get("roleNames"));

			log.info("토큰 갱신 성공: email={}", newClaims.get("email"));
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody);

		} catch (Exception e) {
			log.error("토큰 갱신 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "토큰 갱신 중 오류가 발생했습니다."));
		}
	}

	@PostMapping("/api/auth/signout")
	public ResponseEntity<Map<String, Object>> logout(@AuthenticationPrincipal MemberDto principal,
			HttpServletResponse response) {

		try {
			// 1) refreshToken 쿠키 삭제
			// 쿠키를 삭제하려면 같은 이름의 쿠키를 MaxAge 0으로 설정
			Cookie refreshTokenCookie = new Cookie("refreshToken", null);
			refreshTokenCookie.setHttpOnly(true);
			refreshTokenCookie.setPath("/");
			refreshTokenCookie.setMaxAge(0); // 쿠키 즉시 삭제
			refreshTokenCookie.setAttribute("SameSite", "Lax");
			response.addCookie(refreshTokenCookie);

			// 2) SecurityContext 클리어
			SecurityContextHolder.clearContext();

			String email = principal != null ? principal.getEmail() : "알 수 없음";
			log.info("로그아웃 성공: email={}", email);

			// 3) 성공 응답 반환
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("success", true, "message", "로그아웃되었습니다."));

		} catch (Exception e) {
			log.error("로그아웃 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("success", false, "error", "로그아웃 중 오류가 발생했습니다."));
		}
	}

	@DeleteMapping("/api/auth/delete")
	public ResponseEntity<Map<String, Object>> delete(@AuthenticationPrincipal MemberDto principal,
	                                                  HttpServletResponse response) {

		try {
			// 1) refreshToken 쿠키 삭제
			Cookie refreshTokenCookie = new Cookie("refreshToken", null);
			refreshTokenCookie.setHttpOnly(true);
			refreshTokenCookie.setPath("/");
			refreshTokenCookie.setMaxAge(0); // 쿠키 즉시 삭제
			refreshTokenCookie.setAttribute("SameSite", "Lax");
			response.addCookie(refreshTokenCookie);

			// 2) SecurityContext 클리어
			SecurityContextHolder.clearContext();

			String email = principal != null ? principal.getEmail() : "알 수 없음";
			log.info("회원 탈퇴 요청: email={}", email);

			// 3) 실제 회원 탈퇴 처리
			if (principal != null) {
				memberService.deleteMember(email);
				log.info("회원 탈퇴 성공: email={}", email);
			}

			// 4) 성공 응답 반환
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("success", true, "message", "회원탈퇴 되었습니다."));

		} catch (Exception e) {
			log.error("회원탈퇴 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("success", false, "error", "회원탈퇴 중 오류가 발생했습니다."));
		}
	}

	// 마이페이지 회원정보 읽기
	@GetMapping("/api/mypage")
	public ResponseEntity<?> memberRead(@AuthenticationPrincipal(expression = "username") String email) {
		return ResponseEntity.ok(memberService.getCounselorByEmail(email));
	}
	
	// 마이페이지 회원정보 수정
	@PatchMapping("/api/mypage/modify")
	public ResponseEntity<String> memberModify(@AuthenticationPrincipal(expression = "username") String email,
			@RequestBody MemberModifyDto modifydto) {
		try {
			// 서비스 호출
			memberService.modifyMember(email, modifydto);
			return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수정 중 오류가 발생했습니다.");
		}
	}

	/**
	 * Supabase 로그인 사용자를 member 테이블에 동기화.
	 * Body: { "memberId": "email 또는 UUID", "nickname": "닉네임" }
	 * nickname 생략 시 이메일 앞부분 또는 "user-xxx" 사용.
	 */
	@PostMapping("/api/member/sync")
	public ResponseEntity<?> syncMember(@RequestBody Map<String, String> body) {
		String memberId = body != null ? body.get("memberId") : null;
		String nickname = body != null ? body.get("nickname") : null;
		try {
			Member member = memberService.syncMember(memberId, nickname);
			return ResponseEntity.ok(Map.of(
				"message", "동기화되었습니다.",
				"memberId", member.getMemberId(),
				"nickname", member.getNickname()
			));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
