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
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_id")
	private Member memberId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="cnsler_id")
	private Member cnslerId;
	
	@Column(nullable = false)
	private String role;
	
	@Column(nullable = false)
	private String content;
	
	@CreationTimestamp
	private LocalDateTime created_at;
}
