package com.study.spring.keyword.repository;

import com.study.spring.keyword.entity.SensitiveKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensitiveKeywordRepository extends JpaRepository<SensitiveKeyword, Long> {

    /**
     * 활성화된 키워드만 조회.
     * 기존 DB에서 is_active 컬럼이 NULL 인 경우도 '활성'으로 간주하기 위해
     * (is_active = true OR is_active IS NULL) 조건으로 조회한다.
     */
    @Query("SELECT k FROM SensitiveKeyword k WHERE k.isActive = true OR k.isActive IS NULL")
    List<SensitiveKeyword> findByIsActiveTrue();

    // 키워드 문자열로 조회 (중복 체크용)
    Optional<SensitiveKeyword> findByKeyword(String keyword);

    /**
     * 카테고리별 활성 키워드 조회.
     * 마찬가지로 is_active 가 NULL 인 경우도 활성으로 취급한다.
     */
    @Query("SELECT k FROM SensitiveKeyword k WHERE k.category = :category AND (k.isActive = true OR k.isActive IS NULL)")
    List<SensitiveKeyword> findByCategoryAndIsActiveTrue(@Param("category") String category);
}
