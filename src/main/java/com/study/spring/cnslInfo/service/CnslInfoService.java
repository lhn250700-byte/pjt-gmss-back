package com.study.spring.cnslInfo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.study.spring.Cnsl.dto.cnslPriceDto;
import com.study.spring.cnslInfo.repository.CnslInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CnslInfoService {
	private final CnslInfoRepository cnslInfoRepository;

	public List<cnslPriceDto> findCnslPriceWithTypeName(String email) {
		return cnslInfoRepository.findCnslPriceWithTypeName(email);
	}
	

}
