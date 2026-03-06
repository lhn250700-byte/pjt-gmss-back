package com.study.spring.center.repository;

import com.study.spring.center.entity.SupportCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportCenterRepository extends JpaRepository<SupportCenter, Long> {

    Page<SupportCenter> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
