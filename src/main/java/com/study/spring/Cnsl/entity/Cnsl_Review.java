package com.study.spring.Cnsl.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.study.spring.Member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cnsl_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cnsl_Review {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="review_id")
    private Integer reviewId;
	
	// 1. 상담 신청자 (User와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id",nullable = false)
    private Member memberId;
    
    // 2. 상담사 (User와 N:1, 작성자와 별개로 상담사 역할을 하는 유저)
    // 상담사 ID의경우 Cnsl_reg의 cnslr_id를 사용하면되므로 삭제함 
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="cnsl_id", nullable = false)
	private Cnsl_Reg cnslId;
    
	private String title;
	private String content;
	@Column(name="eval_pt")
	private Integer evalPt;
	@Column(name="del_yn")
	private String delYn = "N";
	@CreationTimestamp
	private LocalDateTime created_at;
	
	@UpdateTimestamp
    private LocalDateTime updated_at;
}
