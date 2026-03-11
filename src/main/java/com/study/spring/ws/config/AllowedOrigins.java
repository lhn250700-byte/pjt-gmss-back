package com.study.spring.ws.config;

import java.util.Arrays;
import java.util.List;

final class AllowedOrigins {

    private AllowedOrigins() {
    }

    static String[] allowedOriginPatterns() {
        String env = System.getenv("CORS_ORIGINS");
        List<String> defaults = List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        );
        List<String> origins = (env == null || env.isBlank())
                ? defaults
                : Arrays.stream(env.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        // STOMP/SockJS는 패턴 기반 허용이 더 유연합니다.
        return origins.toArray(new String[0]);
    }
}

