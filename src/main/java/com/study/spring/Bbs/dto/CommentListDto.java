package com.study.spring.Bbs.dto;

import java.time.LocalDateTime;

public interface CommentListDto {
	Long getCmtId();
	String getBbsDiv();    // as bbsDiv 와 매핑
    String getTitle();
    String getContent();
    String getNickname();
    LocalDateTime getCreatedAt(); // as createdAt 과 매핑
    Long getCmtCount();    // as cmtCount 와 매핑
    Long getClikeCount();  // as clikeCount 와 매핑
	}
