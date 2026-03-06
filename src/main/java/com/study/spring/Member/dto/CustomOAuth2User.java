package com.study.spring.Member.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.study.spring.Member.entity.Member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final Member member;
    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return member.getMemberRoleList().stream()
                .map(role -> new SimpleGrantedAuthority("Role_" + role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return member.getMemberId(); // email
    }

	public String getEmail() {
		return member.getMemberId(); // email
	}

	public String getNickname() {
		return member.getNickname();
	}
	
	public MemberDto getMemberDto() {
	    return new MemberDto(
	            member.getMemberId(),
	            member.getPw(),
	            member.getNickname(),
	            member.isSocial(),
	            member.getMemberRoleList().stream()
	                    .map(Enum::name)
	                    .toList()
	    );
	}

}
