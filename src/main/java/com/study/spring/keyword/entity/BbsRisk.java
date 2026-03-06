package com.study.spring.keyword.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bbs_risk")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BbsRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_id", nullable = false)
    private String tableId;

    @Column(name = "bbs_div", nullable = false)
    private String bbsDiv;

    @Column(name = "bbs_id", nullable = false)
    private Long bbsId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "action")
    private String action;

    /** 감지된 민감 키워드 (쉼표 구분, 관리자 화면 표시용) */
    @Column(name = "detected_keywords", length = 500)
    private String detectedKeywords;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
