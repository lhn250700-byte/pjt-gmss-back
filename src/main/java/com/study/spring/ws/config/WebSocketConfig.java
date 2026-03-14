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
        // accessToken은 쿠키에 넣지 않으므로, 프론트는 연결 시 쿼리 파라미 token 또는 CONNECT 헤더 Authorization: Bearer <token> 전달.
        // 예: SockJS('/ws?token=' + encodeURIComponent(accessToken)) 또는 CONNECT frame에 token/Authorization 헤더.
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

