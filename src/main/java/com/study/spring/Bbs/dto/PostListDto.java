package com.study.spring.Bbs.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostListDto{
	private Integer bbs_id;
	private String bbs_div;
	private String title;
	private Integer views;
	private LocalDateTime created_at;
	private String nickname;
	private Long likeCount; 
}
