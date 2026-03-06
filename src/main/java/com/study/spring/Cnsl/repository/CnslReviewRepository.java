package com.study.spring.Cnsl.repository;

import com.study.spring.Cnsl.entity.Cnsl_Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CnslReviewRepository extends JpaRepository<Cnsl_Review, Integer> {

  @Query("SELECT r FROM Cnsl_Review r WHERE r.delYn = :delYn ORDER BY r.created_at DESC")
  Page<Cnsl_Review> findByDelYnOrderByCreatedAtDesc(@Param("delYn") String delYn, Pageable pageable);

  @Query("SELECT r FROM Cnsl_Review r WHERE r.cnslId.cnslId = :cnslId AND r.delYn = :delYn ORDER BY r.created_at DESC")
  Page<Cnsl_Review> findByCnslIdAndDelYn(@Param("cnslId") Integer cnslId, @Param("delYn") String delYn, Pageable pageable);

  @Query("SELECT r FROM Cnsl_Review r WHERE r.cnslId.cnslId = :cnslId AND r.delYn = :delYn")
  List<Cnsl_Review> findAllByCnslIdAndDelYn(@Param("cnslId") Integer cnslId, @Param("delYn") String delYn);

  @Query("SELECT r FROM Cnsl_Review r WHERE r.memberId.memberId = :memberId AND r.delYn = :delYn ORDER BY r.created_at DESC")
  Page<Cnsl_Review> findByMemberIdAndDelYn(@Param("memberId") String memberId, @Param("delYn") String delYn, Pageable pageable);
}


//@Repository
//public interface CnslReviewRepository extends JpaRepository<Cnsl_Review, Integer> {
//	
//	Page<Cnsl_Review> findByDelYnOrderByCreated_atDesc(String delYn, Pageable pageable);
//
//
////    // 전체 리뷰 목록 (삭제되지 않은 것만)
////    Page<Cnsl_Review> findByDelYnOrderByCreatedAtDesc(String delYn, Pageable pageable);
//
//    // 특정 상담의 리뷰 목록
//    @Query("SELECT r FROM Cnsl_Review r WHERE r.cnslId.cnslId = :cnslId AND r.delYn = :delYn ORDER BY r.created_at DESC")
//    Page<Cnsl_Review> findByCnslIdAndDelYn(@Param("cnslId") Integer cnslId, @Param("delYn") String delYn, Pageable pageable);
//
//    // 특정 상담의 리뷰 (평균 계산용)
//    @Query("SELECT r FROM Cnsl_Review r WHERE r.cnslId.cnslId = :cnslId AND r.delYn = :delYn")
//    List<Cnsl_Review> findAllByCnslIdAndDelYn(@Param("cnslId") Integer cnslId, @Param("delYn") String delYn);
//    
//    // 특정 회원이 작성한 리뷰
//    @Query("SELECT r FROM Cnsl_Review r WHERE r.memberId.memberId = :memberId AND r.delYn = :delYn ORDER BY r.created_at DESC")
//    Page<Cnsl_Review> findByMemberIdAndDelYn(@Param("memberId") String memberId, @Param("delYn") String delYn, Pageable pageable);
//}
