package com.study.spring.Member.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "member", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude="memberRoleList")
public class Member {
	@Id
	@Column(name = "member_id") 
	private String memberId; // email
	private String pw;       // 비밀번호
	private boolean social;
	
	@JsonIgnore // API 응답(예: Bbs 목록의 작성자) 직렬화 시 LazyInitializationException 방지
	@ElementCollection(fetch=FetchType.LAZY)
	@CollectionTable(
			name = "member_role_list",
			schema = "public",
			joinColumns = @JoinColumn(name = "member_member_id")
	)
	@Column(name = "member_role_list") // DB에 실제 값이 저장된 컬럼 (role 컬럼은 null)
	@Enumerated(EnumType.ORDINAL) // 0=USER, 1=SYSTEM, 2=ADMIN (DB: smallint)
	@Builder.Default
	private List<MemberRole> memberRoleList = new ArrayList<>();
	//private String role; // 1. 상담자, 2. 상담사, 3. 관리자 (Code 테이블 'role' 매핑 )

	// nullable=false → 무조건 값 있어야 함
	@Column(nullable = false, unique = true)
	private String nickname;
	private String gender; // M/F (Code 테이블 'gender' 매핑)
	private String mbti; 
	private LocalDate birth; 
	private String persona;
	@Column(name="img_name")
	private String imgName;
	@Column(name="img_url")
	private String imgUrl;	
		
    // 상담사 전용 정보
    private String profile;
    private String text;
    @JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "hash_tags", columnDefinition = "jsonb")
	private Map<String, Object> hashTags;
	private LocalDateTime updatedAt;
	private LocalDateTime createdAt;
	
	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
	
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
	
	public void addRole(MemberRole memberRole) {
		memberRoleList.add(memberRole);
	}
	public void clearRole() {
		memberRoleList.clear();
	}

	public void changePw(String pw) {
		this.pw = pw;
	}

	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}

	public void changeSocial(boolean social) {
		this.social = social;
	}
}