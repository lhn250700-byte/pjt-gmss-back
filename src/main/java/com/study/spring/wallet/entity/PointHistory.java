package com.study.spring.wallet.entity;

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
@Table(name = "point_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_id", nullable = false)
	private Member memberId;
	
	private Long amount;
	@Column(name="point_after")
	private Long pointAfter;
	
    // 상담 ID가 없는 경우도 있으로 연관관계 삭제
//	@ManyToOne
//	@JoinColumn(name = "cnsl_id")
//	private CnslReg cnslId; 
	
    @Column(name = "cnsl_id")
    private Long cnslId;
	private String brief;
	
	@CreationTimestamp
	private LocalDateTime createdAt;

}
