package com.study.spring.Member.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class KakaoSignUpDto {
    private String nickname;
    private String gender;
    private String mbti;
    private LocalDate birth;
    private String persona;
    private String profile;
    private String text;
}
