package com.study.spring.Member.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SignUpDto {
    private String email;
    private String password;
    private String nickname;
    private boolean social;
    private String gender;
    private String mbti;
    private LocalDate birth;
    private String persona;
    private String profile;
    private String text;
}
