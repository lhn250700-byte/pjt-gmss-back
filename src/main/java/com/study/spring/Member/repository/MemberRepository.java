package com.study.spring.Member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.study.spring.Member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, String>{
	
//	member_id를 Email로 검증함
	@Query("""
			select m from Member
			m left join fetch m.memberRoleList
			where m.memberId = :email
			""")
	Optional<Member> findByEmail(@Param("email") String email);

	boolean existsByNickname(String nickname);

	Optional<Member> findByMemberId(String memberId);

}
