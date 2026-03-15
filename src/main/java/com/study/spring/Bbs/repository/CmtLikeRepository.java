package com.study.spring.Bbs.repository;

import com.study.spring.Bbs.entity.Bbs_Comment;
import com.study.spring.Bbs.entity.Cmt_Like;
import com.study.spring.Member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CmtLikeRepository extends JpaRepository<Cmt_Like, Integer> {

    Optional<Cmt_Like> findByCmtIdAndMemberIdMemberId(Bbs_Comment cmt, String memberId);

    List<Cmt_Like> findByCmtId(Bbs_Comment cmt);

    Optional<Cmt_Like> findByMemberId(Member member);

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE Cmt_Like cl
    SET cl.memberId = :delMember
    WHERE cl.memberId = :member
""")
    int updateMember(@Param("member") Member member,
                     @Param("delMember") Member deletedMember);
}
