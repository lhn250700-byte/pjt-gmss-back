package com.study.spring.security.filter;

import com.google.gson.Gson;
import com.study.spring.Member.dto.MemberDto;
import com.study.spring.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Log4j2
public class  JWTCheckFilter extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.info("shouldNotFilter check url................." + path);

//        if (path.startsWith("/api/member/") || path.startsWith("/api/auth/refresh") || path.startsWith("/swagger-ui")) {
//            return true;
//        }

//        if (!path.startsWith("/api/") || path.equals("/api/auth/refresh") || path.equals("/api/member/login") || path.equals("/api/member/signup")) return true;
          if (
//                  !path.startsWith("/api/") ||
                  path.equals("/api/auth/refresh") ||
                  path.equals("/api/member/login") ||
                  path.equals("/api/member/signup") ||
                  path.startsWith("/swagger-ui/") ||
                  path.startsWith("/api-docs/") ||
                  path.startsWith("/api/member_InfoNicknameChk") || 
                  path.equals("/api-docs") ||
                  path.startsWith("/api/bbs") ||
                  path.startsWith("/api/testchatpy")
          ) return true;


        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        log.info("------------------------JWTCheckFilter.......................");
        log.info("Request URI: " + request.getRequestURI());
        log.info("Request Method: " + request.getMethod());

        // OPTIONS 메서드(프리플라이트 요청)는 필터를 통과시킴
        if ("OPTIONS".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeaderStr = request.getHeader("Authorization");
        log.info("Authorization Header: " + (authHeaderStr != null ? authHeaderStr.substring(0, Math.min(50, authHeaderStr.length())) + "..." : "null"));

        String accessToken = null;
        if (authHeaderStr != null && authHeaderStr.startsWith("Bearer ")) {
            accessToken = authHeaderStr.substring(7);
        }
        if ((accessToken == null || accessToken.isBlank()) && request.getCookies() != null) {
            accessToken = Arrays.stream(request.getCookies())
                    .filter(c -> "accessToken".equals(c.getName()))
                    .map(c -> c.getValue())
                    .findFirst()
                    .orElse(null);
        }
        if (accessToken == null || accessToken.isBlank()) {
            Gson gson = new Gson();
            String msg = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN", "message", "accessToken이 없습니다."));
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg);
            printWriter.close();
            return;
        }

        try {
            Map<String, Object> claims = JWTUtil.validateToken(accessToken);

            log.info("JWT claims: " + claims);

            //filterChain.doFilter(request, response); //이하 추가

            String email = (String) claims.get("email");
            String password = (String) claims.get("password");
            String nickname = (String) claims.get("nickname");
            Boolean social = (Boolean) claims.get("social");
            @SuppressWarnings("unchecked")
            List<String> roleNames = (List<String>) claims.get("roleNames");
            
            if (nickname == null) {
                nickname = "kakao_";
            }

            MemberDto memberDto = new MemberDto(email, password, nickname, social.booleanValue(), roleNames);

            log.info("-----------------------------------");
            log.info(memberDto);
            log.info(memberDto.getAuthorities());

            UsernamePasswordAuthenticationToken authenticationToken
                    = new UsernamePasswordAuthenticationToken(memberDto, password, memberDto.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch(Exception e){

            log.error("JWT Check Error..............");
            log.error(e.getMessage());

            Gson gson = new Gson();
            String msg = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN"));

//	      response.setContentType("application/json");
            response.setContentType("application/json;charset=UTF-8"); // 한글이나 문자대응
            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg);
            printWriter.close();

        }
    }
}