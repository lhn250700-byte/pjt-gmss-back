package com.study.spring.ws.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;
    private final WsHandshakeCookieInterceptor wsHandshakeCookieInterceptor;

    public WebSocketConfig(StompAuthChannelInterceptor stompAuthChannelInterceptor,
                           WsHandshakeCookieInterceptor wsHandshakeCookieInterceptor) {
        this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
        this.wsHandshakeCookieInterceptor = wsHandshakeCookieInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 프론트는 쿠키(HttpOnly) 기반이므로 withCredentials + SockJS를 사용합니다.
        // Origin은 환경변수 CORS_ORIGINS(콤마구분) 기준으로 허용합니다.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(AllowedOrigins.allowedOriginPatterns())
                .addInterceptors(wsHandshakeCookieInterceptor)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}

