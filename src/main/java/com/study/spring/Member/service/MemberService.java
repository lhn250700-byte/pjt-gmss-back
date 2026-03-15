package com.study.spring.Member.service;

import com.study.spring.Bbs.entity.Bbs;
import com.study.spring.Bbs.entity.Bbs_Comment;
import com.study.spring.Bbs.entity.Bbs_Like;
import com.study.spring.Bbs.entity.Cmt_Like;
import com.study.spring.Bbs.repository.BbsCommentRepository;
import com.study.spring.Bbs.repository.BbsLikeRepository;
import com.study.spring.Bbs.repository.BbsRepository;
import com.study.spring.Bbs.repository.CmtLikeRepository;
import com.study.spring.Cnsl.entity.Cnsl_Reg;
import com.study.spring.Cnsl.entity.Cnsl_Resp;
import com.study.spring.Cnsl.entity.Cnsl_Review;
import com.study.spring.Cnsl.repository.CnslRepository;
import com.study.spring.Cnsl.repository.CnslRespRepository;
import com.study.spring.Cnsl.repository.CnslReviewRepository;
import com.study.spring.Member.dto.*;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.entity.MemberRole;
import com.study.spring.Member.repository.MemberInfoRepository;
import com.study.spring.Member.repository.MemberRepository;
import com.study.spring.cnslInfo.repository.CnslInfoRepository;
import com.study.spring.keyword.entity.BbsRisk;
import com.study.spring.keyword.repository.BbsRiskRepository;
import com.study.spring.wallet.entity.PointHistory;
import com.study.spring.wallet.entity.Wallet;
import com.study.spring.wallet.repository.PaymentRepository;
import com.study.spring.wallet.repository.PointHistoryRepository;
import com.study.spring.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberInfoRepository memberInfoRepository;
	private final PasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;
	private final WalletRepository walletRepository;
	private final PointHistoryRepository pointHistoryRepository;
	private final CnslInfoRepository cnslInfoRepository;
	private final PaymentRepository paymentRepository;

	private final BbsRepository bbsRepository;
	private final BbsCommentRepository bbsCommentRepository;
	private final BbsLikeRepository bbsLikeRepository;
	private final CmtLikeRepository cmtLikeRepository;
	private final CnslRepository cnslRepository;
	private final CnslRespRepository cnslRespRepository;
	private final CnslReviewRepository cnslReviewRepository;
	private final BbsRiskRepository bbsRiskRepository;

	private static final String DELETED_MEMBER_ID = "deleted@system.local";


	@Transactional(readOnly = true)
	public MemberDto getMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.map(member -> new MemberDto(
				member.getMemberId(), // email
				member.getPw(),       // password
				member.getNickname(),
				member.isSocial(),
				member.getMemberRoleList().stream()
				.map(MemberRole::name) // enum → String
				.collect(Collectors.toList())
			))
			.orElse(null);
	}

	@Transactional
	public void register(SignUpDto request) {
		Optional<MemberInfoEmailCheckDTO> checkResults = memberInfoRepository
				.memberInfoEmailCheckYn(request.getEmail());
		Optional<MemberInfoNicknameCheckDTO> nicknameCheckResult = memberInfoRepository
				.memberInfoNicknameCheckYn(request.getNickname());
		// [소셜 + 계정 존재]
		checkResults.ifPresent(res -> {
			if ("Y".equals(res.getUserInfoEmailCheckYn()) && res.getSocial()) {
				throw new IllegalStateException(
						String.format("소셜 로그인 충돌: 이메일 '%s'은 이미 기존 계정에 연결되어 있습니다.", request.getEmail()));
			} // [계정 존재]
			else if ("Y".equals(res.getUserInfoEmailCheckYn()) && !res.getSocial()) {
				throw new IllegalStateException(
						String.format("일반 회원 가입 충돌: 이메일 '%s'은 이미 사용 중입니다.", request.getEmail()));
			}
		});

		// [닉네임 중복 체크]
		nicknameCheckResult.ifPresent(res -> {
			if ("Y".equals(res.getUserInfoNicknameCheckYn()))
				throw new IllegalStateException(
						String.format("닉네임 중복 : 닉네임 '%s'은(는) 이미 사용 중입니다.", request.getNickname()));
		});

        String encode = passwordEncoder.encode(request.getPassword());
        System.out.println("======================= EMAIL: =======================" + request.getEmail());
        Member member = Member
                .builder()
                .memberId(request.getEmail())
                .pw(encode)
                .nickname(request.getNickname())
                .social(request.isSocial())
                .gender(request.getGender())
                .mbti(request.getMbti())
                .birth(request.getBirth())
                .persona(request.getPersona())
                .profile(request.getProfile())
                .text(request.getText())
                .build();

		member.addRole(MemberRole.USER);
		memberRepository.save(member);

		Wallet wallet = Wallet
				.builder()
				.member(member)
				.currPoint(5000L)
				.build();

		walletRepository.save(wallet);

		PointHistory pointHistory = PointHistory
				.builder()
				.memberId(member)
				.amount(5000L)
				.pointAfter(5000L)
				.cnslId(null)
				.brief("웰컴 포인트")
				.build();

		pointHistoryRepository.save(pointHistory);
	}

	@Transactional
	public void kakaoRegister(String email, KakaoSignUpDto kakaoSignUpDto) {
		Optional<Member> member = memberRepository.findByEmail(email);
		Optional<MemberInfoNicknameCheckDTO> nicknameCheckResult = memberInfoRepository
				.memberInfoNicknameCheckYn(kakaoSignUpDto.getNickname());

		// [닉네임 중복 체크]
		nicknameCheckResult.ifPresent(res -> {
			if ("Y".equals(res.getUserInfoNicknameCheckYn()))
				throw new IllegalStateException(
						String.format("닉네임 중복 : 닉네임 '%s'은(는) 이미 사용 중입니다.", kakaoSignUpDto.getNickname()));
		});

		if (member.isEmpty()) {
			throw new IllegalArgumentException("회원이 존재하지 않습니다.");
		}

		if (kakaoSignUpDto.getNickname() != null) {
			member.get().setNickname(kakaoSignUpDto.getNickname());
		}

		if (kakaoSignUpDto.getGender() != null) {
			member.get().setGender(kakaoSignUpDto.getGender());
		}

		if (kakaoSignUpDto.getMbti() != null) {
			member.get().setMbti(kakaoSignUpDto.getMbti());
		}

		if (kakaoSignUpDto.getBirth() != null) {
			member.get().setBirth(kakaoSignUpDto.getBirth());
		}

		if (kakaoSignUpDto.getPersona() != null) {
			member.get().setPersona(kakaoSignUpDto.getPersona());
		}

		if (kakaoSignUpDto.getProfile() != null) {
			member.get().setProfile(kakaoSignUpDto.getProfile());
		}

		if (kakaoSignUpDto.getText() != null) {
			member.get().setText(kakaoSignUpDto.getText());
		}

		Wallet wallet = Wallet
				.builder()
				.member(member.get())
				.currPoint(5000L)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();

		walletRepository.save(wallet);

		PointHistory pointHistory = PointHistory
				.builder()
				.memberId(member.get())
				.amount(5000L)
				.pointAfter(5000L)
				.cnslId(null)
				.brief("웰컴 포인트")
				.createdAt(LocalDateTime.now())
				.build();

		pointHistoryRepository.save(pointHistory);
	}

	/**
	 * 닉네임 중복 체크
	 * 
	 * @return 중복이면 true, 사용 가능하면 false
	 */
	@Transactional(readOnly = true)
	public boolean isNicknameDuplicated(String nickname) {
		return memberRepository.existsByNickname(nickname);
	}

	/**
	 * email 중복 체크
	 * 
	 * @return 중복이면 true, 사용 가능하면 false
	 */
	@Transactional(readOnly = true)
	public boolean isEmailDuplicated(String email) {
		return false;
	}

	// 회원정보 수정
	@Transactional
	public void modifyMember(String email, MemberModifyDto membermodifydto) {
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다. email : " + email));

		// 에러 발생 지점: 아래에 정의한 메서드를 호출함
		updateCommonInfo(member, membermodifydto);

		// 역할별 분기 로직
		boolean isCounselor = member.getMemberRoleList().contains(MemberRole.SYSTEM);
		if (isCounselor) {
			if (membermodifydto.getProfile() != null)
				member.setProfile(membermodifydto.getProfile());
			if (membermodifydto.getText() != null)
				member.setText(membermodifydto.getText());
			if (membermodifydto.getHashTags() != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("hashTag", membermodifydto.getHashTags());
				member.setHashTags(map);
			}

		} else if (!member.getMemberRoleList().contains(MemberRole.ADMIN)) {
			if (membermodifydto.getMbti() != null)
				member.setMbti(membermodifydto.getMbti());
			if (membermodifydto.getPersona() != null)
				member.setPersona(membermodifydto.getPersona());
		} else {
			if (membermodifydto.getMbti() != null)
				member.setMbti(membermodifydto.getMbti());
			if (membermodifydto.getPersona() != null)
				member.setPersona(membermodifydto.getPersona());
			if (membermodifydto.getPw() != null)
				member.changePw(membermodifydto.getPw()); // set 안하고 changePw로 테스트
			if (membermodifydto.getNickname() != null)
				member.changeNickname(membermodifydto.getNickname());
		}
	}

	// [회원 탈퇴]
	@Transactional
	public void deleteMember(String email) {

		// 1. 삭제 대상 회원 조회
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다. email : " + email));

		// 2. 탈퇴 계정 조회
		Member deletedMember = memberRepository.findByMemberId(DELETED_MEMBER_ID)
				.orElseThrow(() -> new IllegalStateException("탈퇴용 계정이 존재하지 않습니다."));

		// 3. 상담사면 상담정보 삭제
		if (member.getMemberRoleList().contains(1)) {
			cnslInfoRepository.deleteByMemberId(member);
		}

		// 4. 금융 관련 데이터 삭제
		pointHistoryRepository.deleteByMemberId(member);
		paymentRepository.deleteByMemberId(member);
		walletRepository.deleteByMemberId(email);

		// 5. 게시글 / 댓글 / 좋아요 작성자 변경 (벌크 update)
		bbsRepository.updateMember(member, deletedMember);
		bbsCommentRepository.updateMember(member, deletedMember);
		bbsLikeRepository.updateMember(member, deletedMember);
		cmtLikeRepository.updateMember(member, deletedMember);

		cnslRepository.updateMember(member, deletedMember);
		cnslRespRepository.updateMember(member, deletedMember);
		cnslReviewRepository.updateMember(member, deletedMember);

		// 6. 로그 테이블 (String memberId)
		bbsRiskRepository.updateMember(member.getMemberId(), DELETED_MEMBER_ID);

		// 7. 역할 제거
		member.clearRole();

		// 8. 회원 삭제
		memberRepository.delete(member);
	}

	// 이 부분을 추가하면 에러가 사라집니다!
	private void updateCommonInfo(Member member, MemberModifyDto dto) {
		// 1. 닉네임 변경 체크
		if (dto.getNickname() != null && !dto.getNickname().equals(member.getNickname())) {
			if (memberRepository.existsByNickname(dto.getNickname())) {
				throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
			}
			member.changeNickname(dto.getNickname());
		}
		// 2. 비밀번호 변경 체크
		if (dto.getPw() != null && !dto.getPw().isEmpty()) {
			String encodePw = passwordEncoder.encode(dto.getPw());
			member.changePw(encodePw);
		}
	}

	/**
     * Supabase Auth 로그인 사용자를 member 테이블에 동기화.
     * member_id가 이미 있으면 그대로 반환, 없으면 새로 생성 (게시글 작성 등에서 작성자로 사용 가능).
     *
     * @param memberId Supabase user.email 또는 user.id (UUID)
     * @param nickname 표시용 닉네임 (신규 생성 시 필수, unique)
     * @return 기존 또는 새로 생성된 Member
     */
    @Transactional
    public Member syncMember(String memberId, String nickname) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("member_id는 필수입니다.");
        }
        String id = memberId.trim();
        Optional<Member> existing = memberRepository.findById(id);
        if (existing.isPresent()) {
            return existing.get();
        }
        if (nickname == null || nickname.isBlank()) {
            nickname = id.contains("@") ? id.substring(0, id.indexOf('@')) : ("user-" + id.substring(0, Math.min(8, id.length())));
        }
        if (memberRepository.existsByNickname(nickname)) {
            nickname = nickname + "-" + System.currentTimeMillis() % 10000;
        }
        Member newMember = Member.builder()
                .memberId(id)
                .pw("")
                .social(true)
                .nickname(nickname)
                .build();
        newMember.addRole(MemberRole.USER);
        return memberRepository.save(newMember);
    }

	// 상담사 정보 조회
	public Member getCounselorByEmail(String email) {
		return memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
	}
}