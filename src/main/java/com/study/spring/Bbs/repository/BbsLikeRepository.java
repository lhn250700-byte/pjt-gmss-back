package com.study.spring.Bbs.repository;

import com.study.spring.Bbs.entity.Bbs_Like;
import com.study.spring.Member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface BbsLikeRepository extends JpaRepository<Bbs_Like, Integer> {

    @Query("SELECT l FROM Bbs_Like l WHERE l.bbsId.bbsId = :bbsId AND l.memberId.memberId = :memberId")
    Optional<Bbs_Like> findByBbsIdAndMemberId(@Param("bbsId") Integer bbsId, @Param("memberId") String memberId);

    @Query("SELECT l FROM Bbs_Like l WHERE l.bbsId.bbsId = :bbsId")
    List<Bbs_Like> findByBbsId(@Param("bbsId") Integer bbsId);

    Optional<Bbs_Like> findByMemberId(Member member);

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE Bbs_Like l
    SET l.memberId = :delMember
    WHERE l.memberId = :member
""")
    int updateMember(@Param("member") Member member,
                     @Param("delMember") Member deletedMember);
}
