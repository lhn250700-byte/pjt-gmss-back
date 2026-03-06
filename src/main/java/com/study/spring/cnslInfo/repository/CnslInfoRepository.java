package com.study.spring.cnslInfo.repository;

import com.study.spring.Cnsl.dto.cnslPriceDto;
import com.study.spring.cnslInfo.entity.CnslInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CnslInfoRepository extends JpaRepository<CnslInfo, Long> {
    @Query(value = """
        select cnsl_price 
        from cnsl_info 
        where  member_id = :email and cnsl_tp = :cnslTp
    """, nativeQuery = true)
    Long findCnslPrice(@Param("email") String email, @Param("cnslTp") String cnslTp);
    
    @Query(value = """
		select 
    		get_code_nm('cnsl_tp', ci.cnsl_tp) as cnslTypeName, 
    		max(ci.cnsl_price) cnslPrice
		from cnsl_info ci
		where ci.member_id = :email
		group by ci.cnsl_tp
    		""", nativeQuery = true)
    List<cnslPriceDto> findCnslPriceWithTypeName(@Param("email") String email);
}
