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

/**
 * Spring JWT 검사 필터.
 * permitAll 경로 중 "일부 메서드만 로그인 필요"인 경우(GET 공개 / POST·PUT·DELETE 인증)에는
 * 메서드별로 스킵 여부를 나눠서, 쓰기 요청 시에만 JWT를 검사하고 principal을 설정한다.
 */
@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {

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
                  path.equals("/api/auth/login") ||
                  path.equals("/api/member/login") ||
                  path.equals("/api/member/signup") ||
                  path.startsWith("/swagger-ui/") ||
                  path.startsWith("/api-docs/") ||
                  path.startsWith("/api/member_InfoNicknameChk") || 
                  path.equals("/api-docs") ||
                  path.startsWith("/api/testchatpy")
          ) return true;

        // /api/bbs: GET만 JWT 스킵(목록/상세 공개), POST·PUT·DELETE는 JWT 검사하여 작성자 인증
        if (path.startsWith("/api/bbs")) {
            String method = request.getMethod();
            if ("GET".equalsIgnoreCase(method)) {
                log.info("JWT filter skip for path (GET): {}", path);
                return true;
            }
            // POST, PUT, DELETE → JWT 필터 통과하여 principal 설정
        }

        // 공개 API 및 Swagger 관련 경로는 JWT 체크 제외
        if (
                path.equals("/") ||
                path.equals("/api/auth/refresh") ||
                path.equals("/api/auth/login") ||
                path.equals("/api/member/login") ||
                path.equals("/api/member/signup") ||
                path.startsWith("/api/member_InfoNicknameChk") ||
                path.startsWith("/swagger-ui/") ||
                path.equals("/swagger-ui") ||
                path.startsWith("/api-docs/") ||
                path.equals("/api-docs") ||
                path.equals("/api/centers") ||
                path.startsWith("/api/centers/") ||
                path.equals("/api/bbs_popularPostRealtimeList")
        ) {
            log.info("JWT filter skip for path: {}", path);
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        log.info("------------------------JWTCheckFilter.......................");
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Request Method: {}", request.getMethod());

        // OPTIONS 요청은 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeaderStr = request.getHeader("Authorization");
        log.info("Authorization Header: {}",
                authHeaderStr != null
                        ? authHeaderStr.substring(0, Math.min(50, authHeaderStr.length())) + "..."
                        : "null");

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

            log.info("JWT claims: {}", claims);

            String email = (String) claims.get("email");
            String password = (String) claims.get("password");
            String nickname = (String) claims.get("nickname");
            Boolean social = (Boolean) claims.get("social");
            @SuppressWarnings("unchecked")
            List<String> roleNames = (List<String>) claims.get("roleNames");

            if (nickname == null) {
                nickname = "kakao_";
            }

            MemberDto memberDto = new MemberDto(
                    email,
                    password,
                    nickname,
                    social != null && social,
                    roleNames
            );

            log.info("-----------------------------------");
            log.info(memberDto);
            log.info(memberDto.getAuthorities());

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            memberDto,
                            password,
                            memberDto.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT Check Error..............");
            log.error(e.getMessage(), e);

            Gson gson = new Gson();
            String msg = gson.toJson(Map.of(
                    "error", "ERROR_ACCESS_TOKEN",
                    "message", "유효하지 않은 Access Token입니다."
            ));

            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg);
            printWriter.close();
        }
    }
}