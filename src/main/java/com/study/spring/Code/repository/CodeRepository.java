package com.study.spring.Code.repository;

import com.study.spring.Code.entity.Code;
import com.study.spring.Code.dto.CodeDto; // 방금 만드신 인터페이스
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeRepository extends JpaRepository<Code, String> {

	// 1. 코드 단건조회
	// col_id 와 code를 던지면 code, code_name을 return 함
    @Query(value = """
           SELECT code, 
                  code_name AS codeName 
             FROM code 
            WHERE col_id = :colId 
              AND code = :code
           """, nativeQuery = true)
    Optional<CodeDto> findCodeDtoByColIdAndCode(@Param("colId") String colId, @Param("code") String code);
    
    // 2. 코드리스트 조회
    // 2. 리스트 조회 (추가): col_id만 조건으로 사용
    @Query(value = """
           SELECT code, 
                  code_name AS codeName 
             FROM code 
            WHERE col_id = :colId
           """, nativeQuery = true)
    List<CodeDto> findCodeListByColId(@Param("colId") String colId);
}