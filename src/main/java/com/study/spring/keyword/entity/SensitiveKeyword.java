package com.study.spring.keyword.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensitive_keywords")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensitiveKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Long keywordId;

    @Column(name = "keyword", unique = true, nullable = false, length = 100)
    private String keyword;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "severity")
    private Integer severity;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at;
}
