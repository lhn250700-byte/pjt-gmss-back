package com.study.spring.wallet.entity;

import java.time.LocalDateTime;
import com.study.spring.Member.entity.Member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {
	@Id
	@Column(name = "member_id")
	private String memberId;   // PK

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;   // FK
	
	@Column(name = "curr_point")
	private Long currPoint = 0L; // 현재 포인트

	private LocalDateTime updatedAt;
	private LocalDateTime createdAt;
	
	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
	
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public void addPoint(Long amount) {
		if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.currPoint += amount;
	}
		
}
