package com.study.spring.security.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.google.gson.Gson;
import com.study.spring.Member.dto.CustomOAuth2User;
import com.study.spring.Member.dto.MemberDto;
import com.study.spring.util.JWTUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

//        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
//
//        // OAuth 유저 → 우리 서비스용 DTO 변환
//        MemberDto memberDto = new MemberDto(
//                oAuth2User.getEmail(),
//                "social-login",               // 패스워드는 의미 없음
//                oAuth2User.getNickname(),
//                true,
//                List.of("USER")
//        );
//
//        Map<String, Object> claims = memberDto.getClaims();
//
//        // JWT 생성
//        String accessToken = JWTUtil.generateToken(claims, 10);
//        String refreshToken = JWTUtil.generateToken(claims, 60 * 24);
//
//        // refreshToken 쿠키 저장
//        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
//        refreshTokenCookie.setHttpOnly(true);
//        refreshTokenCookie.setPath("/");
//        refreshTokenCookie.setMaxAge(60 * 60 * 24);
//        refreshTokenCookie.setAttribute("SameSite", "Lax");
//
//        response.addCookie(refreshTokenCookie);
//
//        claims.put("accessToken", accessToken);
//
//        // JSON 응답
//        Gson gson = new Gson();
//        String jsonStr = gson.toJson(claims);
//
//        response.setContentType("application/json;charset=UTF-8");
//        PrintWriter writer = response.getWriter();
//        writer.println(jsonStr);
//        writer.close();
    }
}
