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
@Table(name = "cmt_like")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cmt_Like {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="clike_id")
	private Integer clikeId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="cmt_id", nullable = false)
	private Bbs_Comment cmtId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="member_id",nullable = false)
	private Member memberId;

	@Column(name="is_like")
	private boolean isLike;

	@CreationTimestamp
	private LocalDateTime createdAt;
}
