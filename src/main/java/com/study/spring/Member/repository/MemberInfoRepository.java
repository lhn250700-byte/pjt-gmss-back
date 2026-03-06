package com.study.spring.Member.repository;

import com.study.spring.Member.dto.MemberInfoEmailCheckDTO;
import com.study.spring.Member.dto.MemberInfoNicknameCheckDTO;
import com.study.spring.Member.entity.Member;

import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository 사용 가능
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberInfoRepository extends JpaRepository<Member, String> {

    // DTO 인터페이스를 반환 타입으로 지정
    @Query(value = """
            SELECT m.social as social, CASE WHEN COUNT(*) > 0 THEN 'Y' ELSE 'N' END AS userInfoEmailCheckYn
            FROM member m
            WHERE m.member_id = :email
            group by m.social
            """,
            nativeQuery = true)
    Optional<MemberInfoEmailCheckDTO> memberInfoEmailCheckYn(@Param("email") String email);
    
    // DTO 인터페이스를 반환 타입으로 지정
    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN 'Y' ELSE 'N' END AS userInfoNicknameCheckYn
            FROM member
            WHERE nickname = :nickname
            """,
            nativeQuery = true)
    Optional<MemberInfoNicknameCheckDTO> memberInfoNicknameCheckYn(@Param("nickname") String nickname);
}
