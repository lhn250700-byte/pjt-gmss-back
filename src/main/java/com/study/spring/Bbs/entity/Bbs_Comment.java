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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "bbs_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bbs_Comment {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer cmt_id;
	
	// (게시글과 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="bbs_id")
    private Bbs bbsId;

    // (사용자와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    private Member memberId;

	private String content;
	@Column(name="del_yn")
	private String delYn = "N";
	
	@CreationTimestamp
	private LocalDateTime created_at;
    @UpdateTimestamp
    private LocalDateTime updated_at;
}
