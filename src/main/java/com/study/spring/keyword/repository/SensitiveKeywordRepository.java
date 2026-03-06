package com.study.spring.keyword.repository;

import com.study.spring.keyword.entity.SensitiveKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensitiveKeywordRepository extends JpaRepository<SensitiveKeyword, Long> {

    // 활성화된 키워드만 조회
    List<SensitiveKeyword> findByIsActiveTrue();

    // 키워드 문자열로 조회 (중복 체크용)
    Optional<SensitiveKeyword> findByKeyword(String keyword);

    // 카테고리별 조회
    List<SensitiveKeyword> findByCategoryAndIsActiveTrue(String category);
}
