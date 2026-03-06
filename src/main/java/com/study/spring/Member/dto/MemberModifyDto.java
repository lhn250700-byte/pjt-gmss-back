package com.study.spring.Member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberModifyDto {
	private String nickname;
	private String pw;
	private String persona;
	private String mbti;
	private String profile; // 상담사 전용
    private String text; // 상담사 전용
}
