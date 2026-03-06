package com.study.spring.Member.service;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.study.spring.Member.dto.MemberDto;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CustomUserDetailService implements UserDetailsService {
	
	@Autowired
	private MemberRepository memberRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// username은 email임
		// {}의 경우 변수출력
		log.info("--------------- loadUserByUsername 호출, username:{} --------------", username);
		
		Optional<Member> member = memberRepository.findByEmail(username);
		
		log.info("이메일 조회 결과 = username:{}, member.isPresent():{}", username, member.isPresent());
		
		if (member.isEmpty()) {
			log.warn("사용자를 찾을 수 없습니다.");
			throw new UsernameNotFoundException("사용자를 찾을수 없습니다.");
		}
		
		// entity -> DTO
		MemberDto memberDto = new MemberDto(
			    member.get().getMemberId(),
			    member.get().getPw(),
			    member.get().getNickname(),
			    member.get().isSocial(),
			    member.get().getMemberRoleList().stream() // 리스트에서 스트림 생성
			        .map(memberRole -> memberRole.name()) // Enum을 String으로 변환
			        .collect(Collectors.toList())         // 다시 List로 수집
				);
		log.info(memberDto);		
		
		return memberDto;
	}

	
}
