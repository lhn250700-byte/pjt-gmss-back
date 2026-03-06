package com.study.spring.Cnsl.repository;

import com.study.spring.Cnsl.entity.Cnsl_Resp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CnslRespRepository extends JpaRepository<Cnsl_Resp, Long> {

}
