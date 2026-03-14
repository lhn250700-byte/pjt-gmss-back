package com.study.spring.Member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.study.spring.Member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, String>{
	
//	member_id를 Email로 검증함 (fetch 제거 시 member_role_list 스키마 이슈로 500 방지; getMemberByEmail에서 @Transactional로 lazy 로딩)
	@Query("select m from Member m where m.memberId = :email")
	Optional<Member> findByEmail(@Param("email") String email);

	boolean existsByNickname(String nickname);

	Optional<Member> findByMemberId(String memberId);


}
