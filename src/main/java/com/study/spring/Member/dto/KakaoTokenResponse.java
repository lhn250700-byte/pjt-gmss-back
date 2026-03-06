package com.study.spring.Member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenResponse(

        @JsonProperty("access_token") String accessToken,

        @JsonProperty("token_type") String tokenType,

        @JsonProperty("refresh_token") String refreshToken,

        @JsonProperty("expires_in") Long expiresIn,

        @JsonProperty("scope") String scope) {
}
