package com.study.spring.keyword.repository;

import com.study.spring.keyword.entity.BbsRisk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BbsRiskRepository extends JpaRepository<BbsRisk, Long> {

    // 최신순
    @Query("SELECT b FROM BbsRisk b ORDER BY b.createdAt DESC")
    Page<BbsRisk> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 특정 기간 이후
    @Query("SELECT b FROM BbsRisk b WHERE b.createdAt > :after ORDER BY b.createdAt DESC")
    List<BbsRisk> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("after") LocalDateTime after);

    // 특정 게시물
    @Query("SELECT b FROM BbsRisk b WHERE b.bbsId = :bbsId ORDER BY b.createdAt DESC")
    List<BbsRisk> findByBbsIdOrderByCreatedAtDesc(@Param("bbsId") Long bbsId);

    Optional<BbsRisk> findByMemberId(String memberId);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE BbsRisk br
        SET br.memberId = :delMemberId
        WHERE br.memberId = :memberId
    """)
    int updateMember(@Param("memberId") String memberId,
                     @Param("delMemberId") String deletedMemberId);
}
