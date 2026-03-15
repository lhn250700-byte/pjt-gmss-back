package com.study.spring.Bbs.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.study.spring.Member.entity.Member;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bbs")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Bbs {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // pk, increment 대응
    @Column(name="bbs_id")
    private Integer bbsId;

	@Column(nullable = false)
    private String bbs_div; // (Code 테이블 'bbs_div' 매핑)

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member memberId;
	
	private String mbti;
	private String title;
	private String content;
	private Integer views = 0;
	@Column(name="img_name")
	private String imgName;
	@Column(name="img_url")
	private String imgUrl;	
	@Column(name="del_yn")
	private String delYn = "N";

	/** INSERT 시 del_yn이 null이면 'N'으로 설정 (JSON 역직렬화 등으로 null이 들어와도 DB에는 N 저장) */
	@PrePersist
	public void prePersist() {
		if (this.delYn == null || this.delYn.isBlank()) {
			this.delYn = "N";
		}
	}

	@CreationTimestamp
    @Column
    private LocalDateTime created_at;

	@UpdateTimestamp
    private LocalDateTime updated_at;
	
	// @Column(name = "hash_tags", columnDefinition = "jsonb", insertable = false, updatable = false)
	// private String hashTags;

	// Vector 처리:pgvector-java 라이브러리 등. 목록/상세 API 응답에서 제외(용량·선택적 컬럼 대비)
	@JsonIgnore
	@Column(columnDefinition = "vector(1536)")
	private float[] embedding;
}
