package com.study.spring.config;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.OpenAPI;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

	// 1. CORS 설정 부분
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // /api로 시작하는 모든 경로에 적용
                // allowCredentials(true)와 함께 사용할 패턴 설정
                .allowedOriginPatterns("http://localhost:*") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // OPTIONS 메서드 추가 (Preflight 대비)
                .allowedHeaders("*") // 모든 헤더 허용 (필요시 특정 헤더만 나열)
                .exposedHeaders("Location", "Set-Cookie") // 클라이언트가 읽을 수 있는 응답 헤더 추가
                .allowCredentials(true) // 인증 정보(쿠키, 인증 헤더 등) 포함 허용
                .maxAge(3600); // Preflight 요청 결과 캐싱 시간 (1시간)

        // Swagger 관련 경로에 대한 CORS 허용 (필요 시)
        registry.addMapping("/v3/api-docs/**")
                .allowedOriginPatterns("*");
    }
    // 2. Swagger(OpenAPI) 이름 및 정보 설정 부분 추가
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GMSS API 명세서") // Swagger UI에 표시될 제목
                        .description("백엔드 API 서버 문서입니다.") // 제목 아래 설명
                        .version("v1.0.0")); // 서비스 버전
    }
}
