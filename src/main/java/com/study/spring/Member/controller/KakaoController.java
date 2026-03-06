//package com.study.spring.Member.controller;
//
//import java.util.Map;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//@RequiredArgsConstructor
//@RestController
//@Log4j2
//public class KakaoController {
//
//    @GetMapping("/kakao/auth-code")
//    public void loginForm(
//            @RequestParam(required = false) String code,
//            @RequestParam(required = false) String error,
//            @RequestParam(name = "error_description", required = false) String errorDescription,
//            @RequestParam(required = false) String state
//    ){
//        System.out.println("### kakao 인가 코드 요청");
//        System.out.println("code: " + code);
//        System.out.println("error: " + error);
//        System.out.println("errorDescription: " + errorDescription);
//        System.out.println("state: " + state);
//        // 인가 코드
//        String authCode = code;
//        // 토큰 응답
//        KakaoTokenResponse tokenResponse = getAccessToken(authCode);
//        // 사용자 정보 응답
//        KakaoUserInfoResponse userInfo = getUserInfo(tokenResponse.accessToken());
//
//        log.info("------------------- 토큰 응답 -------------------");
//        log.info(tokenResponse);
//        log.info("------------------- 사용자 정보 응답 -------------------");
//    }
//
//    private KakaoTokenResponse getAccessToken(String authCode) {
//        final String KAKAO_CLIENT_ID = "my-client-id";
//        final String KAKAO_CLIENT_SECRET = "my-client-secret";
//        final String REDIRECT_URI = "http://localhost:8080/kakao/auth-code"; // my-redirect-uri
//        // 헤더
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//        // body
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("grant_type", "authorization_code");
//        body.add("client_id", KAKAO_CLIENT_ID);
//        body.add("redirect_uri", REDIRECT_URI);
//        body.add("code", authCode);
//        body.add("client_secret", KAKAO_CLIENT_SECRET);
//        // Http요청 객체
//        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);
//        // Kakao API 호출
//        ResponseEntity<KakaoTokenResponse> response =
//                new RestTemplate().exchange(
//                        "https://kauth.kakao.com/oauth/token",
//                        HttpMethod.POST,
//                        httpEntity,
//                        KakaoTokenResponse.class);
//
//        return response.getBody();
//    }
//
//    private KakaoUserInfoResponse getUserInfo(String accessToken){
//        final String BEARER_TOKEN_PREFIX = "Bearer ";
//        // 헤더
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//        headers.add("Authorization", BEARER_TOKEN_PREFIX + accessToken);
//        // Http요청 객체
//        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
//        // Kakao API 호출
//        ResponseEntity<KakaoUserInfoResponse> response =
//                new RestTemplate().exchange(
//                        "https://kapi.kakao.com/v2/user/me",
//                        HttpMethod.GET,
//                        new HttpEntity<>(headers),
//                        KakaoUserInfoResponse.class);
//
//        return response.getBody();
//    }
//
//    public record KakaoTokenResponse(
//            @JsonProperty("access_token")
//            String accessToken,
//
//            @JsonProperty("token_type")
//            String tokenType,
//
//            @JsonProperty("refresh_token")
//            String refreshToken,
//
//            @JsonProperty("id_token")
//            String idToken,
//
//            @JsonProperty("expires_in")
//            Long expiresIn,
//
//            @JsonProperty("scope")
//            String scope,
//
//            @JsonProperty("refresh_token_expires_in")
//            Long refreshTokenExpiresIn
//    ) {}
//
//    public record KakaoUserInfoResponse(
//            @JsonProperty("id")
//            Long id,
//
//            @JsonProperty("connected_at")
//            String connectedAt,
//
//            @JsonProperty("kakao_account")
//            KakaoAccount kakaoAccount,
//
//            @JsonProperty("properties")
//            Map<String, Object> properties
//    ) {
//
//        public record KakaoAccount(
//                @JsonProperty("profile_nickname_needs_agreement")
//                Boolean profileNicknameNeedsAgreement,
//
//                @JsonProperty("profile_image_needs_agreement")
//                Boolean profileImageNeedsAgreement,
//
//                @JsonProperty("profile")
//                Profile profile
//        ) {
//
//            public record Profile(
//                    @JsonProperty("nickname")
//                    String nickname,
//
//                    @JsonProperty("thumbnail_image_url")
//                    String thumbnailImageUrl,
//
//                    @JsonProperty("profile_image_url")
//                    String profileImageUrl,
//
//                    @JsonProperty("is_default_image")
//                    Boolean isDefaultImage,
//
//                    @JsonProperty("is_default_nickname")
//                    Boolean isDefaultNickname
//            ) {
//            }
//        }
//    }
//}