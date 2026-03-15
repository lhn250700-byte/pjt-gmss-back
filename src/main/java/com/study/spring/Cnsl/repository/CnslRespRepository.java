package com.study.spring.Cnsl.repository;

import com.study.spring.Cnsl.entity.Cnsl_Resp;
import com.study.spring.Member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CnslRespRepository extends JpaRepository<Cnsl_Resp, Long> {
    Optional<Cnsl_Resp> findByMemberId(Member member);

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE Cnsl_Resp r
    SET r.memberId = :delMember
    WHERE r.memberId = :member
""")
    int updateMember(@Param("member") Member member,
                     @Param("delMember") Member deletedMember);
}
