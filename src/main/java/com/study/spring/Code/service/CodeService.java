package com.study.spring.Code.service;

import com.study.spring.Code.dto.CodeDto;
import com.study.spring.Code.repository.CodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeRepository codeRepository;

    // 단건 조회: codeName만 반환하거나 없으면 null 반환
    public String getCodeName(String colId, String code) {
        return codeRepository.findCodeDtoByColIdAndCode(colId, code)
                .map(CodeDto::getCodeName)
                .orElse("Not Found");
    }

    // 리스트 조회: colId에 해당하는 모든 code, codeName 반환
    public List<CodeDto> getCodeList(String colId) {
        return codeRepository.findCodeListByColId(colId);
    }
}