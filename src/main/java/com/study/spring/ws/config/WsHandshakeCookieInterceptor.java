package com.study.spring.ws.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 핸드셰이크 시 accessToken 수집.
 * accessToken은 쿠키에 넣지 않으므로, 쿼리 파라미 token 또는 (레거시) accessToken 쿠키에서 읽음.
 */
@Component
public class WsHandshakeCookieInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest r = servletRequest.getServletRequest();
            // 1) 쿼리 파라미 token 우선 (accessToken 쿠키 미사용 정책)
            String token = r.getParameter("token");
            if (token == null || token.isBlank()) {
                // 2) 레거시: 쿠키에서 accessToken
                Cookie[] cookies = r.getCookies();
                if (cookies != null) {
                    for (Cookie c : cookies) {
                        if ("accessToken".equals(c.getName())) {
                            token = c.getValue();
                            break;
                        }
                    }
                }
            }
            if (token != null && !token.isBlank()) {
                attributes.put("accessToken", token);
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}

