package com.study.spring.Bot.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name="bot_msg")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bot_Msg {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
	private Integer bot_id;
	
	@Column(nullable = false)
	private Integer session_id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member memberId;
	
	/**
     * 3. 봇의 응답 데이터 (jsonb)
     * 메시지 텍스트, 버튼 구성, 시나리오 ID 등 가변적인 데이터를 담습니다.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> msg_data;

	private String summary;
	
	@CreationTimestamp
	private LocalDateTime created_at;
}
