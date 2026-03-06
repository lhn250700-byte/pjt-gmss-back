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
@Table(name = "cnsl_resp")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cnsl_Resp {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="resp_id")
    private Long respId;
	
	// (상담신청과 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cnsl_id" ,nullable = false)
    private Cnsl_Reg cnslId;

    // (사용자와 N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id", nullable = false)
    private Member memberId;
	
	private String content;

    @Column(name="del_yn")
    private String delYn = "N";
	
	@CreationTimestamp
    @Column(updatable = false)
	// 한번 저장된 데이터는 수정할 때까지 건드리지 않는다.
    private LocalDateTime created_at;
}
