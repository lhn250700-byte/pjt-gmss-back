package com.study.spring.Bbs.entity;

import java.time.LocalDateTime;
import java.util.Map;

import com.study.spring.Member.entity.Member;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

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
	
	@CreationTimestamp
    @Column
    private LocalDateTime created_at;

	@UpdateTimestamp
    private LocalDateTime updated_at;
	
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "hash_tags", columnDefinition = "jsonb")
	private Map<String, Object> hashTags;

	// Vector 처리:pgvector-java 라이브러리 등을 사용하거나 float[]로 매핑합니다.
	@Column(columnDefinition = "vector(1536)")
	private float[] embedding;



	// [성진님 작성 코드] 
	// /** 벡터(임베딩) - DB에는 네이티브 쿼리로 저장/조회. JPA에서는 제외. */
	// @Transient
	// private float[] embedding;

	// /** DB bbs.mbti NOT NULL 대응: 저장 직전에 null이면 빈 문자열로 설정 */
	// @PrePersist
	// public void ensureMbtiNotNull() {
	// 	if (this.mbti == null) {
	// 		this.mbti = "";
	// 	}
	// }
}
