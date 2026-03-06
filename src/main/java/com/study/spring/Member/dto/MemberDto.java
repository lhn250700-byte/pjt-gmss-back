package com.study.spring.Member.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MemberDto extends User {

	private String email;
	private String pw;
	private String nickname;
	private boolean social;
//	private List<MemberRole> memberRoleList = new ArrayList<>();
	private List<String> roleNames = new ArrayList<>();
	
	public MemberDto(String email, String pw, String nickname, boolean social, List<String> roleNames ) {
		// super : User에서 상속받음 : 
		// super(username, password, authorities);
		super(email, pw, roleNames.stream()
				.map(str -> new SimpleGrantedAuthority("Role_"+str)).collect(Collectors.toList()));
		
		this.email = email;
		this.pw = pw;
		this.nickname = nickname;
		this.social = social;
		this.roleNames = roleNames;
	}

	// 토큰에 담길내용을 넣음(Map type으로 return : key:value형태)
	public Map<String, Object> getClaims() {
		
		Map<String, Object> dataMap = new HashMap<>();
		
		dataMap.put("email", email);
//		dataMap.put("password", pw);
		dataMap.put("nickname", nickname);
		dataMap.put("social", social);
		dataMap.put("roleNames", roleNames);
		
		return dataMap;
	}
}
