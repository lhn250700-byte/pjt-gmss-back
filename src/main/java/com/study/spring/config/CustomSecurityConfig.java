	package com.study.spring.config;

	import java.util.List;

	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
	import org.springframework.security.config.annotation.web.builders.HttpSecurity;
	import org.springframework.security.config.http.SessionCreationPolicy;
	import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
	import org.springframework.security.crypto.password.PasswordEncoder;
	import org.springframework.security.web.SecurityFilterChain;
	import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
	import org.springframework.web.cors.CorsConfiguration;
	import org.springframework.web.cors.CorsConfigurationSource;
	import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

	import com.study.spring.Member.service.CustomOAuth2UserService;
	import com.study.spring.security.filter.JWTCheckFilter;
	import com.study.spring.security.handler.APILoginFailHandler;
	import com.study.spring.security.handler.APILoginSuccessHandler;

	import lombok.RequiredArgsConstructor;
	import lombok.extern.log4j.Log4j2;

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
		public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
			log.info("---------------------security config---------------------------");

			http.csrf(config -> config.disable());
	//		http.cors(config -> config.disable());
			http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
			http.sessionManagement(sessionConfig ->  sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

			http.formLogin(config -> {
				  config.loginPage("/api/member/login");
				  config.successHandler(new APILoginSuccessHandler());
				  config.failureHandler(new APILoginFailHandler());

			});

			 http.authorizeHttpRequests(auth -> auth.requestMatchers( "/",
			 "/api/member/signup", "/api/member/login", "/api/auth/refresh",
			 "/api/auth/signout", "/api/member_InfoNicknameChk", "/api/member/**",
			 "/api/auth/**", "/api/centers", "/api/centers/**", "/api/bbs**", "/api/bbs/**").permitAll()
			 .anyRequest().authenticated());

			// 일반 로그인 필터
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
			CorsConfiguration config = new CorsConfiguration();

			config.setAllowedOriginPatterns(List.of("*")); // 모든 Origin 허용

			// config.setAllowedOrigins(
			// 		List.of(
			// 				"http://127.0.0.1:5173",
			// 				"http://localhost:5173"
			// 				)
			// 		);


			config.setAllowCredentials(true);              // 반드시 false 쿠키인증이 필요시 true
			config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
			config.setAllowedHeaders(List.of("*"));  // 모든 헤더 허용 (CORS 프리플라이트 요청 처리)
			config.setExposedHeaders(List.of("Authorization"));  // 클라이언트에서 접근 가능한 헤더
			config.setMaxAge(3600L);  // 프리플라이트 요청 캐시 시간 (1시간)

			UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
			source.registerCorsConfiguration("/**", config);
			return source;
		}
	}
