package com.study.spring.Member.entity; // entity 패키지 생성

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
// Supabase의 auth 스키마에 있는 users 테이블과 매핑
@Table(name = "users", schema = "auth") 
public class AuthUser {
    @Id
    private UUID id;
    private String email;
    // 다른 필드는 생략 가능
}