package com.study.spring.ws.config;

import com.study.spring.Cnsl.entity.Cnsl_Reg;
import com.study.spring.Cnsl.repository.CnslRepository;
import com.study.spring.Member.dto.MemberDto;
import com.study.spring.util.JWTUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern TOPIC_CNSL_CHAT = Pattern.compile("^/topic/cnsl/(\\d+)/(chat|stat)$");

    private final CnslRepository cnslRepository;

    public StompAuthChannelInterceptor(CnslRepository cnslRepository) {
        this.cnslRepository = cnslRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand cmd = accessor.getCommand();
        if (cmd == null) return message;

        if (cmd == StompCommand.CONNECT) {
            Authentication auth = authenticateFromCookie(accessor);
            if (auth == null) {
                throw new IllegalStateException("UNAUTHORIZED");
            }
            accessor.setUser(auth);
            SecurityContextHolder.getContext().setAuthentication(auth);
            return message;
        }

        if (cmd == StompCommand.SUBSCRIBE) {
            Authentication auth = (Authentication) accessor.getUser();
            if (auth == null) auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof MemberDto principal)) {
                throw new IllegalStateException("UNAUTHORIZED");
            }

            String dest = accessor.getDestination();
            if (dest == null) return message;

            Matcher m = TOPIC_CNSL_CHAT.matcher(dest);
            if (!m.matches()) return message; // 다른 토픽은 여기서 제약하지 않음

            long cnslId = Long.parseLong(m.group(1));
            Cnsl_Reg cnsl = cnslRepository.findById(cnslId).orElse(null);
            if (cnsl == null) throw new IllegalStateException("NOT_FOUND");

            String email = principal.getEmail();
            String memberEmail = cnsl.getMemberId() != null ? cnsl.getMemberId().getMemberId() : null;
            String cnslerEmail = cnsl.getCnslerId() != null ? cnsl.getCnslerId().getMemberId() : null;
            boolean allowed = Objects.equals(email, memberEmail) || Objects.equals(email, cnslerEmail);
            if (!allowed) throw new IllegalStateException("FORBIDDEN");

            return message;
        }

        return message;
    }

    /** accessToken은 쿠키에 넣지 않으므로, Handshake 시 쿼리 param 또는 CONNECT 헤더에서 조회 */
    private Authentication authenticateFromCookie(StompHeaderAccessor accessor) {
        // 1) Handshake 시 쿼리 param(token) 또는 레거시 쿠키로 넣은 session attribute
        Object tokenFromSession = accessor.getSessionAttributes() != null ? accessor.getSessionAttributes().get("accessToken") : null;
        String token = tokenFromSession != null ? String.valueOf(tokenFromSession) : null;
        if (token != null && !token.isBlank()) {
            return authenticateByJwt(token);
        }

        // 2) CONNECT 프레임 헤더: Authorization Bearer 또는 token
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String v = authHeaders.get(0);
            if (v != null && v.startsWith("Bearer ")) {
                token = v.substring(7).trim();
                if (!token.isBlank()) return authenticateByJwt(token);
            }
        }
        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            token = tokenHeaders.get(0);
            if (token != null && !token.isBlank()) return authenticateByJwt(token);
        }

        // 3) 레거시: Cookie 헤더에서 accessToken
        List<String> cookieHeaders = accessor.getNativeHeader("cookie");
        if (cookieHeaders == null || cookieHeaders.isEmpty()) cookieHeaders = accessor.getNativeHeader("Cookie");
        if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
            String cookie = String.join("; ", cookieHeaders);
            token = extractCookieValue(cookie, "accessToken");
            if (token != null && !token.isBlank()) return authenticateByJwt(token);
        }
        return null;
    }

    private Authentication authenticateByJwt(String token) {
        try {
            Map<String, Object> claims = JWTUtil.validateToken(token);
            String email = Objects.toString(claims.get("email"), "");
            String password = Objects.toString(claims.get("password"), "");
            String nickname = Objects.toString(claims.get("nickname"), "kakao_");
            boolean social = Boolean.TRUE.equals(claims.get("social"));
            @SuppressWarnings("unchecked")
            List<String> roleNames = (List<String>) claims.get("roleNames");
            if (roleNames == null) roleNames = List.of();
            MemberDto memberDto = new MemberDto(email, password, nickname, social, roleNames);
            return new UsernamePasswordAuthenticationToken(memberDto, password, memberDto.getAuthorities());
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractCookieValue(String cookieHeader, String cookieName) {
        if (cookieHeader == null || cookieHeader.isBlank()) return null;
        String[] parts = cookieHeader.split(";");
        for (String p : parts) {
            String s = p.trim();
            int eq = s.indexOf('=');
            if (eq <= 0) continue;
            String name = s.substring(0, eq).trim();
            if (!cookieName.equals(name)) continue;
            return s.substring(eq + 1).trim();
        }
        return null;
    }
}

