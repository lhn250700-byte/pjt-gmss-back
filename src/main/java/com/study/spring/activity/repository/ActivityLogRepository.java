package com.study.spring.activity.repository;

import com.study.spring.activity.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // 전체 활동 로그 (최신순)
    @Query("SELECT a FROM ActivityLog a ORDER BY a.created_at DESC")
    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 특정 회원의 활동 로그
    @Query("SELECT a FROM ActivityLog a WHERE a.memberId = :memberId ORDER BY a.created_at DESC")
    Page<ActivityLog> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") String memberId, Pageable pageable);

    // 특정 기간 이후 활동 로그
    @Query("SELECT a FROM ActivityLog a WHERE a.created_at > :after ORDER BY a.created_at DESC")
    List<ActivityLog> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("after") LocalDateTime after);
    
    // 특정 활동 타입 조회
    @Query("SELECT a FROM ActivityLog a WHERE a.actionType = :actionType ORDER BY a.created_at DESC")
    List<ActivityLog> findByActionTypeOrderByCreatedAtDesc(@Param("actionType") String actionType);
}
