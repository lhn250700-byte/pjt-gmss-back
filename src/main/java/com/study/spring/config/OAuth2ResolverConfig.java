package com.study.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

/**
 * 카카오 OAuth2 인가 요청 리졸버 빈 정의.
 * CustomSecurityConfig에서 주입받으므로 여기서 선언해 순환 참조를 제거한다.
 */
@Configuration
public class OAuth2ResolverConfig {

    @Bean
    public OAuth2AuthorizationRequestResolver kakaoAuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new KakaoAuthorizationRequestResolver(clientRegistrationRepository);
    }
}
