//package com.study.spring.Bbs.entity;
//
//import java.sql.Timestamp;
//import java.time.LocalDateTime;
//
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import com.study.spring.Member.entity.Member;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.Table;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Table(name = "bbs_risk")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Bbs_Risk {
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Integer id;
//
//	@Column(name="table_id")
//	private String tableId; // 테이블명
//	@Column(name="bbs_div")
//	private String bbsDiv; // 테이블분류
//	@Column(name="bbs_id")
//	private Integer bbsId; // 게시물id
//	private String content; // 게시물 내용
//
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name="member_id", nullable = false)
//	private Member memberId;
//
//	private String action; // 조치내용
//
//	@CreationTimestamp
//	private Timestamp createdAt;
//
//	@UpdateTimestamp
//	private LocalDateTime updatedAt;
//}