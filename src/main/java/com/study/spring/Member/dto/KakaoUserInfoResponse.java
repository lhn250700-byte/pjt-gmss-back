package com.study.spring.Member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(

        @JsonProperty("id") Long id,

        @JsonProperty("connected_at") String connectedAt,

        @JsonProperty("kakao_account") KakaoAccount kakaoAccount) {

    public record KakaoAccount(

            @JsonProperty("email") String email,

            @JsonProperty("is_email_valid") Boolean isEmailValid,

            @JsonProperty("is_email_verified") Boolean isEmailVerified) {

        public record Profile() {}
    }
}
