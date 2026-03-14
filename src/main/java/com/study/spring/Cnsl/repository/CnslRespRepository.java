package com.study.spring.Cnsl.repository;

import com.study.spring.Cnsl.entity.Cnsl_Resp;
import com.study.spring.Member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CnslRespRepository extends JpaRepository<Cnsl_Resp, Long> {
    Optional<Cnsl_Resp> findByMemberId(Member member);
}
