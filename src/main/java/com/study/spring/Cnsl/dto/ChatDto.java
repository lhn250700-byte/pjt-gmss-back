package com.study.spring.Cnsl.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDto {
	private Integer chatId;
	private Integer cnslId;
	private String memberId;
	private String cnslerId;
	private String role;
	private String content;
	private LocalDateTime created_at;
}
