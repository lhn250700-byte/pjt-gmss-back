package com.study.spring.config; // 이 줄을 반드시 추가하세요!

import com.study.spring.Member.service.CustomOAuth2UserService;
import com.study.spring.security.filter.JWTCheckFilter;
import com.study.spring.security.handler.APILoginFailHandler;
import com.study.spring.security.handler.APILoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@Log4j2
@EnableMethodSecurity
@RequiredArgsConstructor
public class CustomSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthorizationRequestResolver kakaoAuthorizationRequestResolver;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { // 메서드 선언 필수
        log.info("-------------------security config---------------------------");

        // CORS 설정 적용
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 세션 관리: Stateless 설정 (JWT 사용 시 필수)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // CSRF 비활성화 (API 서버인 경우)
        http.csrf(csrf -> csrf.disable());

        // 로그인 설정
        http.formLogin(config -> {
            config.loginPage("/api/member/login");
            config.loginProcessingUrl("/api/member/login");
            config.successHandler(new APILoginSuccessHandler());
            config.failureHandler(new APILoginFailHandler());
        });

        // 권한 설정 (중복 제거 및 하나로 통합)
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/",
                "/api/member/signup",
                "/api/member/login",
                "/api/auth/refresh",
                "/api/auth/signout",
                "/api/member_InfoNicknameChk",
                "/api/member/**",
                "/api/auth/**",
                "/api/centers",
                "/api/centers/**",
                "/api/bbs**",
                "/api/bbs/**",
                "/api/bbs_popularPostRealtimeList",
                "/api/counselorList",
                "/api/counselor/**",
                "/api/testchatpy/**"
            ).permitAll()
            .anyRequest().authenticated()
        );

        // JWT 필터 추가
        http.addFilterBefore(new JWTCheckFilter(), UsernamePasswordAuthenticationFilter.class);

        // OAuth2 로그인 설정 (카카오는 PKCE 미지원으로 400 방지 위해 커스텀 리졸버 사용)
        http.oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(auth -> auth
                .authorizationRequestResolver(kakaoAuthorizationRequestResolver)
            )
            .userInfoEndpoint(userInfo ->
                userInfo.userService(customOAuth2UserService)
            )
            .successHandler(new APILoginSuccessHandler())
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(java.util.List.of(
            "https://www.gmss.site",
            "https://gmss.site",
            "https://testchat-alpha.vercel.app",
            "http://localhost:5173",
            "http://localhost:3000"
        ));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept", "Origin"));
        config.setExposedHeaders(java.util.List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}