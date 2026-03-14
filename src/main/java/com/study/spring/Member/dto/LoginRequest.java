package com.study.spring.Member.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username; // 이메일
    private String password;
}
