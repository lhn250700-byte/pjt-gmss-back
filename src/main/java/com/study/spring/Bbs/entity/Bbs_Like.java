package com.study.spring.Bbs.entity;

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
@Table(name = "bbs_like")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bbs_Like {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="like_id")
	private Integer likeId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="bbs_id", nullable= false)
	private Bbs bbsId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_id", nullable = false)
	private Member memberId;
	
	@Column(name="is_like")
	private Boolean isLike;
	
	@CreationTimestamp
	private LocalDateTime created_at;
}
