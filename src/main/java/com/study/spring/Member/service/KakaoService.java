package com.study.spring.Member.service;
import com.study.spring.Member.dto.KakaoTokenResponse;
import com.study.spring.Member.dto.KakaoUserInfoResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
public class KakaoService {
    public void kakaoLogin(String code) {
        // 1. 인가 코드 로그
        log.info("인가 코드: " + code);

        // 2. 인가 코드 → 토큰
        KakaoTokenResponse token = getAccessToken(code);
        log.info("access_token: " + token.accessToken());

        // 3. 토큰 → 사용자 정보
        KakaoUserInfoResponse userInfo = getUserInfo(token.accessToken());
        log.info("kakaoId: " + userInfo.id());
        log.info("kakaoEmail: " + userInfo.kakaoAccount().email());
        log.info("kakaoEmailValid " + userInfo.kakaoAccount().isEmailValid());
    }

    // [카카오 토큰 요청]
    private KakaoTokenResponse getAccessToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "db84a36a76acabde3b54a4323ecec380");
        body.add("redirect_uri", "http://localhost:8080/kakao/auth-code");
        body.add("code", code);
        body.add("client_secret", "NcLc5uPCtDjEA8dixgMsm3UntN6dywp2"); // 토큰 발급 시 보안을 강화하기 위한 시크릿 코드

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity("https://kauth.kakao.com/oauth/token", request, KakaoTokenResponse.class);

        return response.getBody();
    }

    // [카카오 유저 정보 요청]
    private KakaoUserInfoResponse getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();


        ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, KakaoUserInfoResponse.class);

        return response.getBody();
    }
}
