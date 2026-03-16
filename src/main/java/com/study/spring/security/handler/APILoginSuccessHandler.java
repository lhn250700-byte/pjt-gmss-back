package com.study.spring.security.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.google.gson.Gson;
import com.study.spring.Member.dto.CustomOAuth2User;
import com.study.spring.Member.dto.MemberDto;
import com.study.spring.util.JWTUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class APILoginSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		log.info("----------------login success-------------------");
		log.info(authentication.getPrincipal());
		log.info("----------------login success-------------------");
		
		Object principal = authentication.getPrincipal(); // principal에 실제 로그인한 사용자의 객체 저장
		MemberDto memberDto;

		boolean isSocialLogin = false;
		
		// 일반 로그인
		if (principal instanceof MemberDto dto) {
			memberDto = dto;
			log.info("일반 로그인 성공: {}", dto.getEmail());
		}
		// 카카오 로그인
		else if (principal instanceof CustomOAuth2User oAuth2User) {
			memberDto = oAuth2User.getMemberDto();
			isSocialLogin = true;
			log.info("소셜 로그인 성공: {}", memberDto.getEmail());
		}
		else throw new RuntimeException("지원하지 않는 Principal 타입: " + principal);
		
		Map<String, Object> claims = memberDto.getClaims();
		
		String accessToken = JWTUtil.generateToken(claims,10);
		String refreshToken = JWTUtil.generateToken(claims,60*24);
		// accessToken은 응답 body만 사용, 쿠키에는 refreshToken만 저장
		jakarta.servlet.http.Cookie refreshTokenCookie = 
				new jakarta.servlet.http.Cookie("refreshToken", refreshToken);
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge(60*60*24);
		refreshTokenCookie.setAttribute("SameSite", "None"); // SameStie = Strict,Lax,None
		refreshTokenCookie.setSecure(true);
		response.addCookie(refreshTokenCookie);

		claims.put("accessToken",accessToken);

		// 소셜 로그인 → 추가정보 입력 페이지 이동
		if (isSocialLogin) {
			String frontendBase = System.getenv("FRONTEND_BASE_URL");
			if (frontendBase == null || frontendBase.isBlank()) {
				frontendBase = "https://www.gmss.site";
			}
			if (frontendBase.endsWith("/")) frontendBase = frontendBase.substring(0, frontendBase.length() - 1);

			if (memberDto.getNickname().startsWith("social_")) {
				log.info(memberDto);
				response.sendRedirect(frontendBase + "/member/kakao-additional");
				return;
			}

			response.sendRedirect(frontendBase);
			return;
		}

		Gson gson = new Gson();

		String jsonStr= gson.toJson(claims); // claims + accessToken을 json 문자열로 변환

		response.setContentType("application/json;charset=UTF-8");
		PrintWriter printWriter = response.getWriter();
		printWriter.println(jsonStr);
		printWriter.close();
	}

}
