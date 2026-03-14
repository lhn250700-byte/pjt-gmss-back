@Configuration
@Log4j2
@EnableMethodSecurity
@RequiredArgsConstructor
public class CustomSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
                "/api/testchatpy/**"
            ).permitAll()
            .anyRequest().authenticated()
        );

        // JWT 필터 추가
        http.addFilterBefore(new JWTCheckFilter(), UsernamePasswordAuthenticationFilter.class);

        // OAuth2 로그인 설정
        http.oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo ->
                userInfo.userService(customOAuth2UserService)
            )
            .successHandler(new APILoginSuccessHandler())
        );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // ... 기존 CORS 설정 코드와 동일 ...
        CorsConfiguration config = new CorsConfiguration();
        // (생략)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}