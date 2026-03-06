package com.study.spring.Code.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.spring.Code.dto.CodeDto;
import com.study.spring.Code.service.CodeService;

@RestController
public class CodeController {
//  private final CodeService codeService;
	@Autowired	
	CodeService codeservice;
	
	@GetMapping("/api/code")
	public String Codetest() {
		return "Code TEST!!!";
	}
	
	// col_id , code 단건조회("code_name?colId="A"&code="B"
	@GetMapping("/api/code_name")
    public String getCodeName(@RequestParam("colId") String colId, 
    						  @RequestParam("code") String code) {
		return codeservice.getCodeName(colId, code);
	}
	
	// col_id 목록조회("code_name?colId="A"
    @GetMapping("/api/code_list")
    public List<CodeDto> getCodeList(@RequestParam("colId") String colId) {
        return codeservice.getCodeList(colId);
    }

}