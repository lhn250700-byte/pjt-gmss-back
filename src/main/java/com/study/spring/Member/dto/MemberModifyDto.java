package com.study.spring.Member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
	private String text; // 상담사 한 줄 소개글
	private List<String> hashTags;
}
