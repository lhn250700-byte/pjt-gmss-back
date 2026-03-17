package com.study.spring.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 카카오 authorize 400 방지: 카카오가 code_challenge/code_challenge_method를 받으면 400을 반환할 수 있어
 * kakao registration에 한해 PKCE 파라미터를 제거한 요청을 반환합니다.
 */
public class KakaoAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String REGISTRATION_ID_KAKAO = "kakao";

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public KakaoAuthorizationRequestResolver(ClientRegistrationRepository repository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
            repository,
            "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request);
        return stripPkceForKakao(authRequest, registrationIdFromRequest(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, clientRegistrationId);
        return stripPkceForKakao(authRequest, clientRegistrationId);
    }

    private String registrationIdFromRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        // /oauth2/authorization/kakao -> kakao
        String prefix = "/oauth2/authorization/";
        if (path != null && path.startsWith(prefix)) {
            String id = path.substring(prefix.length());
            int slash = id.indexOf('/');
            return slash > 0 ? id.substring(0, slash) : id;
        }
        return null;
    }

    private OAuth2AuthorizationRequest stripPkceForKakao(OAuth2AuthorizationRequest request, String registrationId) {
        if (request == null || !REGISTRATION_ID_KAKAO.equals(registrationId)) {
            return request;
        }
        // 카카오: code_challenge, code_challenge_method 제거 (미지원 시 400 방지)
        Map<String, Object> params = new HashMap<>(request.getAdditionalParameters());
        params.remove("code_challenge");
        params.remove("code_challenge_method");

        // 토큰 요청 시 code_verifier 미전송 (PKCE 미사용)
        Map<String, Object> attrs = new HashMap<>(request.getAttributes());
        attrs.remove("code_verifier");

        Set<String> scopes = request.getScopes() != null
            ? request.getScopes().stream().collect(Collectors.toSet())
            : Set.of();

        return OAuth2AuthorizationRequest.authorizationCode()
            .clientId(request.getClientId())
            .authorizationUri(request.getAuthorizationUri())
            .redirectUri(request.getRedirectUri())
            .scopes(scopes)
            .state(request.getState())
            .additionalParameters(params)
            .attributes(attrs)
            .build();
    }
}
