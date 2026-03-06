package com.study.spring.Member.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthAttributes {

    private Map<String, Object> attributes; // 원본 attributes
    private String email;
    private String provider;
    private String providerId;

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {

        if ("kakao".equals(registrationId)) {
            return ofKakao(attributes);
        }

        throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
    		// attributes의 원 타입이 Map<String, Object>이고, value인 attributes.get("kakao_account");가 Object라 Map<String, Object> 형태로 캐스팅
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String email = (String) kakaoAccount.get("email");

        String providerId = String.valueOf(attributes.get("id"));

        return OAuthAttributes.builder()
                .email(email)
                .provider("kakao")
                .providerId(providerId)
                .attributes(attributes)
                .build();
    }

}
