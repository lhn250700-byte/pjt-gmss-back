package com.study.spring.cnslInfo.repository;

import com.study.spring.cnslInfo.entity.CnslerSchd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CnslerSchdRepository extends JpaRepository<CnslerSchd, Integer> {
    @Query(value = "SELECT * FROM cnsler_schd  WHERE member_id = :email", nativeQuery = true)
    Optional<CnslerSchd> findScheduleByEmail(@Param("email") String email);
}
