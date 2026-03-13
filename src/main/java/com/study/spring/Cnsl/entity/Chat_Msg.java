package com.study.spring.Cnsl.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.study.spring.Member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_msg")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat_Msg {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_id")
	private Integer chatId;
	
	@Column(name="cnsl_id", nullable = false)
	private Integer cnslId;
	
	// Supabase에서 관리하는 chat_msg는 Spring JPA로 직접 사용하지 않는다.
	// 과거 구조(단일 content 컬럼)는 제거되었고, 현재는 msg_data(JSONB)를 사용하므로
	// 이 엔티티는 더 이상 DB 스키마와 일치하지 않는다.
	// 남아 있는 JPA 메타데이터 때문에 content 컬럼을 조회하는 SQL이 생성되므로,
	// 전체 엔티티를 비활성화한다.
}
