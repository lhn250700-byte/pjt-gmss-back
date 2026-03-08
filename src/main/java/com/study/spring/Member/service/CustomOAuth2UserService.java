package com.study.spring.Member.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.study.spring.Member.repository.MemberInfoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.spring.Member.dto.CustomOAuth2User;
import com.study.spring.Member.dto.OAuthAttributes;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.entity.MemberRole;
import com.study.spring.Member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        // provider (kakao)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 카카오 attributes 변환
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oAuth2User.getAttributes());

        // DB 저장 또는 조회
        Member member = saveOrUpdate(attributes);

        return new CustomOAuth2User(member, attributes.getAttributes());
    }

    private Member saveOrUpdate(OAuthAttributes attributes) {

        Optional<Member> optionalMember = memberRepository.findByEmail(attributes.getEmail());

        if(optionalMember.isPresent()) {
            // [기존 유저 + 일반 회원인 경우 : 에러 발생]
            if (!optionalMember.get().isSocial()) throw new OAuth2AuthenticationException("이미 해당 이메일로 가입된 계정이 존재합니다.");
            // [기존 유저 + 소셜 회원인 경우 : 로그인 계속]
            else return optionalMember.get();
        }

        // 신규 회원 → 자동 가입
        Member member = Member.builder()
                .memberId(attributes.getEmail())
                .pw("kakao")
                .nickname("social_" + UUID.randomUUID().toString().substring(0,8))
                .social(true)
                .build();

        member.addRole(MemberRole.USER);

        return memberRepository.save(member);
    }

}