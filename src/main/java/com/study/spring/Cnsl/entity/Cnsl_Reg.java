package com.study.spring.Cnsl.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="cnsl_reg")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Cnsl_Reg {
	@Id
	@Column(name="cnsl_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cnslId; // 상담 고유 ID (PK)
	
	// 상담 신청자 (User와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member memberId;
    
    // 상담사 (User와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cnsler_id")
    private Member cnslerId;
    
    @Column(name="cnsl_tp")
	private String cnslTp; // 상담 유형 (Code 테이블 'cnsl_tp' 매핑)
    @Column(name="cnsl_cate")
	private String cnslCate; // 상담 카테고리 (Code 테이블 'cnsl_cate' 매핑)
    @Column(name="cnsl_dt")
	private LocalDate cnslDt; // 상담일자
    @Column(name="cnsl_start_time")
	private LocalTime cnslStartTime; // 상담시작시간
    @Column(name="cnsl_end_time")
	private LocalTime cnslEndTime; // 상담종료시간
    @Column(name="cnsl_stat")
	private String cnslStat = "A"; // 상담상태
    @Column(name="cnsl_title")
	private String cnslTitle; // 상담제목
    @Column(name="cnsl_content")
	private String cnslContent; // 상담신청내용
	
    @Column(name="cnsl_todo_yn")
	private String cnslTodoYn = "Y";// 상담진행여부
    
    @Column(name="del_yn")
    private String delYn = "N";

    @CreationTimestamp
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
    private LocalDateTime updatedAt;
}
