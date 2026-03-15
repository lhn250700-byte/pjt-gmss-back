package com.study.spring.Bbs.repository;

import com.study.spring.Bbs.entity.Bbs_Comment;
import com.study.spring.Member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface BbsCommentRepository extends JpaRepository<Bbs_Comment, Integer> {

    /** 댓글 목록 - memberId 한 번에 로딩해 N+1 제거 (직렬화 시 Lazy 로딩 방지) */
    @EntityGraph(attributePaths = {"memberId"})
    @Query("SELECT c FROM Bbs_Comment c WHERE c.bbsId.bbsId = :bbsId AND c.delYn = 'N' ORDER BY c.created_at ASC")
    List<Bbs_Comment> findByBbsIdAndDelYnOrderByCreatedAtAsc(@Param("bbsId") Integer bbsId);

    Optional<Bbs_Comment> findByMemberId(Member member);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Bbs_Comment c
        SET c.memberId = :delMember
        WHERE c.memberId = :member
    """)
    int updateMember(@Param("member") Member member,
                     @Param("delMember") Member deletedMember);
}
