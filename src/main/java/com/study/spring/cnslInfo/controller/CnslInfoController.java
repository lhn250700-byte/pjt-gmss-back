package com.study.spring.cnslInfo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.spring.Cnsl.dto.cnslPriceDto;
import com.study.spring.cnslInfo.service.CnslInfoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CnslInfoController {
	private final CnslInfoService cnslInfoService;
	
    // [상담사 금액 + 상담 유형]
	@GetMapping("/api/cnslInfo_getPrice")
	public List<cnslPriceDto> getCnslPriceWithTypeName(@RequestParam("email") String email) {
		return cnslInfoService.findCnslPriceWithTypeName(email);
	}
}
