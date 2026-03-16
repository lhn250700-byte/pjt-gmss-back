package com.study.spring.Cnsl.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.lang.String;

import com.study.spring.Cnsl.entity.Cnsl_Resp;
import com.study.spring.Cnsl.entity.CounselingStatus;
import com.study.spring.Cnsl.dto.*;
import com.study.spring.Cnsl.repository.CnslRespRepository;
import com.study.spring.cnslInfo.entity.CnslerSchd;
import com.study.spring.cnslInfo.repository.CnslInfoRepository;
import com.study.spring.cnslInfo.repository.CnslerSchdRepository;
import com.study.spring.wallet.entity.PointHistory;
import com.study.spring.wallet.entity.Wallet;
import com.study.spring.wallet.repository.PointHistoryRepository;
import com.study.spring.wallet.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.study.spring.Cnsl.entity.Cnsl_Reg;
import com.study.spring.Cnsl.repository.CnslRepository;
import com.study.spring.Member.entity.Member;
import com.study.spring.Member.repository.MemberRepository;
import com.study.spring.keyword.service.KeywordService;

import jakarta.transaction.Transactional;

@Service
@Slf4j
public class CnslService {
    @Autowired
    private CnslRepository cnslRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private CnslInfoRepository cnslInfoRepository;
    @Autowired
    private PointHistoryRepository pointHistoryRepository;
    @Autowired
    private CnslerSchdRepository cnslerSchdRepository;
    @Autowired
    private CnslRespRepository cnslRespRepository;
    @Autowired
    private KeywordService keywordService;

    // [상담 예약]
    @Transactional
    public Long reserveCounseling(CnslReqDto cnslReqDto) {
        Member member = memberRepository.findByEmail(cnslReqDto.getMember_id())
                .orElseThrow(() -> new IllegalArgumentException("사용자가 없습니다."));
        Member counselor = memberRepository.findByEmail(cnslReqDto.getCnsler_id())
                .orElseThrow(() -> new IllegalArgumentException("상담사가 없습니다."));

        // [상담 시간 밸리데이션]
        if (cnslReqDto.getCnsl_date().isBefore(LocalDate.now()) ||
                (cnslReqDto.getCnsl_date().equals(LocalDate.now())
                        && cnslReqDto.getCnsl_start_time().isBefore(LocalTime.now())))
            throw new IllegalArgumentException("과거 시간은 예약할 수 없습니다.");

        // 이 날에 예약이 있는지 확인
        Optional<IsCnslDto> yn = isCounseling(member.getMemberId(), counselor.getMemberId(), cnslReqDto.getCnsl_date(),
                cnslReqDto.getCnsl_start_time());
        yn.ifPresent(res -> {
            if ("Y".equals(res.getIsCounselingYn())) {
                throw new IllegalStateException("이미 진행 중인 상담이 존재합니다.");
            }
        });
        
        // 유저의 MBTI·Persona 설정 여부 (null-safe)
        if (member.getMbti() == null || member.getMbti().trim().isEmpty())
            throw new IllegalStateException("상담 진행을 위해 사용자 MBTI 정보가 필요합니다.");
        if (member.getPersona() == null || member.getPersona().trim().isEmpty())
            throw new IllegalStateException("상담 진행을 위해 사용자 Persona(소개) 정보가 필요합니다.");

        // [상담사 영업 시간에 대한 에러 처리]
        CnslerSchd cnslerSchd = cnslerSchdRepository.findScheduleByEmail(cnslReqDto.getCnsler_id())
                .orElseThrow(() -> new IllegalArgumentException("상담 영업 정보가 없습니다."));
        LocalTime startTime = LocalTime.parse(cnslerSchd.getStartTime());
        LocalTime endTime = LocalTime.parse(cnslerSchd.getEndTime());
        if (cnslReqDto.getCnsl_start_time().isBefore(startTime))
            throw new IllegalStateException("영업 시간보다 전 시간입니다.");
        if (cnslReqDto.getCnsl_start_time().isAfter(endTime))
            throw new IllegalStateException("영업 시간 이후 시간입니다.");
        if (cnslReqDto.getCnsl_start_time().equals(endTime))
            throw new IllegalStateException("마감 시간 전으로 예약해 주시길 바랍니다.");

        // [당일 예약 시 시간 제한 3시간]
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime consultationTime = LocalDateTime.of(
                cnslReqDto.getCnsl_date(),
                cnslReqDto.getCnsl_start_time());

        if (!now.isBefore(consultationTime.minusHours(3))) {
            throw new IllegalStateException("상담 시작 3시간 전까지만 예약 가능합니다.");
        }

        // [현재 잔액 get]
        Wallet wallet = walletRepository.findByEmail(cnslReqDto.getMember_id())
                .orElseThrow(() -> new IllegalArgumentException("지갑 정보가 없습니다."));
        Long currPoint = wallet.getCurrPoint();

        // [해당 상담사의 상담 금액 get]
        Long cnslPrice = cnslInfoRepository.findCnslPrice(cnslReqDto.getCnsler_id(), cnslReqDto.getCnsl_tp());
        if (cnslPrice == null)
            throw new IllegalStateException("해당 상담사의 선택한 상담 방식 요금 정보가 없습니다.");
        // [포인트 잔액 부족 시 에러 처리]
        if (currPoint < cnslPrice)
            throw new IllegalStateException("보유 포인트가 상담 금액보다 부족합니다.");

        Cnsl_Reg cnslReg = Cnsl_Reg.builder()
                .cnslerId(counselor)
                .memberId(member)
                .cnslCate(cnslReqDto.getCnsl_cate())
                .cnslTp(cnslReqDto.getCnsl_tp())
                .cnslTitle(cnslReqDto.getCnsl_title())
                .cnslContent(cnslReqDto.getCnsl_content())
                .cnslDt(cnslReqDto.getCnsl_date())
                .cnslStartTime(cnslReqDto.getCnsl_start_time())
                .cnslEndTime(cnslReqDto.getCnsl_start_time().plusHours(1))
                .cnslStat("A")
                .cnslTodoYn("Y")
                .delYn("N")
                .build();

        cnslRepository.save(cnslReg);

        // 민감 키워드 자동 검사 (상담 신청: 제목+내용)
        try {
            String fullContent = (cnslReqDto.getCnsl_title() != null ? cnslReqDto.getCnsl_title() : "") + " " +
                    (cnslReqDto.getCnsl_content() != null ? cnslReqDto.getCnsl_content() : "");
            List<java.util.Map<String, Object>> detected = keywordService.detectSensitiveKeywords(fullContent);
            if (!detected.isEmpty()) {
                keywordService.recordRiskPost(
                        "cnsl_reg",
                        "CNSL",
                        cnslReg.getCnslId(),
                        fullContent,
                        detected,
                        member.getMemberId()
                );
                log.warn("⚠️ 민감 키워드 감지된 상담 신청: cnslId={}, 키워드 개수={}", cnslReg.getCnslId(), detected.size());
            }
        } catch (Exception e) {
            log.warn("민감 키워드 상담 신청 감지/기록 실패: cnslId={}, err={}", cnslReg.getCnslId(), e.getMessage());
        }

        // [포인트 내역 생성]
        PointHistory pointHistory = PointHistory
                .builder()
                .memberId(member)
                .amount(-1 * cnslPrice)
                .pointAfter(currPoint - cnslPrice)
                .cnslId(cnslReg.getCnslId())
                .brief(counselor.getNickname() + " 상담 신청")
                .build();

        pointHistoryRepository.save(pointHistory);

        // [포인트 지갑 입력]
        wallet.setCurrPoint(currPoint - cnslPrice);
        walletRepository.save(wallet);

        return cnslReg.getCnslId();
    }

    /** AI 즉시 상담 등록 (cnsl_tp=3, 상담사 없음). 마이페이지 목록에 노출되도록 동일 DB에 저장 */
    @Transactional
    public Long reserveCounselingAi(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        Member member = memberRepository.findByEmail(memberId.trim())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. (member_id=" + memberId + ")"));
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime endTime = now.plusHours(1);

        Cnsl_Reg cnslReg = Cnsl_Reg.builder()
                .memberId(member)
                .cnslerId(null) // AI 상담은 상담사 없음. DB의 cnsler_id 컬럼이 NULL 허용이어야 함.
                .cnslCate("1")
                .cnslTp("3")
                .cnslTitle("AI 즉시 상담")
                .cnslContent(null)
                .cnslDt(today)
                .cnslStartTime(now)
                .cnslEndTime(endTime)
                .cnslStat("C")
                .cnslTodoYn("Y")
                .delYn("N")
                .build();
        try {
            cnslRepository.save(cnslReg);
        } catch (Exception e) {
            log.error("AI 상담 cnsl_reg 저장 실패 memberId={}", memberId, e);
            throw new IllegalStateException(
                    "상담 등록 저장에 실패했습니다. DB의 cnsl_reg.cnsler_id가 NULL 허용인지 확인하세요. " + e.getMessage());
        }
        return cnslReg.getCnslId();
    }

    /** 진행 중 AI 상담 ID 조회 (없으면 null) */
    public Long findActiveAiCnslId(String memberId) {
        return cnslRepository
                .findTopByMemberId_MemberIdAndCnslTpAndCnslStatOrderByCnslIdDesc(memberId, "3", "C")
                .map(Cnsl_Reg::getCnslId)
                .orElse(null);
    }

    // 신청 시 밸리데이션 체크
    public Optional<IsCnslDto> isCounseling(String memberId, String cnslerId, LocalDate cnsl_date,
            LocalTime cnslStartTime) {
        return cnslRepository.isCounseling(memberId, cnslerId, cnsl_date, cnslStartTime);

    }

    // [상담사 특정 일자 예약 리스트] : 이걸 통해 프론트에서 해당 일자에 상담 가능한 시간을 보여줄 거임
    public List<CnslerDateDto> getAvailableSlotsForCnsler(String cnslerId, LocalDate cnslDt) {
        List<CnslerDateDto> results = cnslRepository.getReservedInfo(cnslerId, cnslDt);

        results.forEach(r -> log.info("날짜: {}, 시작시간: {}, 회원닉네임: {}",
                r.getCnslDt(),
                r.getCnslStartTime(),
                r.getNickname()));

        return results;
    }

    // [상담 수정]
    @Transactional
    public Long modifyMyCounseling(Long cnslId, CnslModiReqDto cnslModiReqDto) {
        // [cnslId 존재 여부 확인]
        Cnsl_Reg cnsl_Reg = cnslRepository.findById(cnslId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담입니다."));

        // [상담 수정 가능 여부 확인 (status = A 아닐 때 or delYn = Y일 때 터짐]
        if (!"A".equals(cnsl_Reg.getCnslStat()) || "Y".equals(cnsl_Reg.getDelYn()))
            throw new IllegalStateException("수정 불가능한 상담 상태입니다.");

        // 시간/일자 변경이 있는 경우에만 영업 시간·수정 가능 시간 체크
        if (cnslModiReqDto.getCnsl_date() != null || cnslModiReqDto.getCnsl_start_time() != null) {
            // [해당 상담사의 영업 시간 중 예약 가능한 시간] - DTO가 아닌 기존 예약의 상담사 ID 기준
            String cnslerId = cnsl_Reg.getCnslerId() != null ? cnsl_Reg.getCnslerId().getMemberId() : null;
            CnslerSchd cnslerSchd = cnslerSchdRepository.findScheduleByEmail(cnslerId)
                    .orElseThrow(() -> new IllegalArgumentException("상담 영업 정보가 없습니다."));
            LocalTime startTime = LocalTime.parse(cnslerSchd.getStartTime());
            LocalTime endTime = LocalTime.parse(cnslerSchd.getEndTime());

            LocalTime newStartTime = cnslModiReqDto.getCnsl_start_time() != null
                    ? cnslModiReqDto.getCnsl_start_time()
                    : cnsl_Reg.getCnslStartTime();

            if (newStartTime.isBefore(startTime))
                throw new IllegalStateException("영업 시간보다 전 시간입니다.");
            if (newStartTime.isAfter(endTime))
                throw new IllegalStateException("영업 시간 이후 시간입니다.");
            if (newStartTime.equals(endTime))
                throw new IllegalStateException("마감 시간 전으로 예약해 주시길 바랍니다.");

            // [상담 수정 가능 시간 유효성 체크] - 날짜 변경이 있는 경우에만 검사
            LocalDate newDate = cnslModiReqDto.getCnsl_date() != null
                    ? cnslModiReqDto.getCnsl_date()
                    : cnsl_Reg.getCnslDt();
            if (newDate.equals(LocalDate.now()))
                throw new IllegalStateException("수정은 하루 전까지만 가능합니다.");
        }

        // patch 작동 방식에 의해 null이면 기존 값 유지, null이 아니면 새로운 값 세팅
        if (cnslModiReqDto.getCnsl_date() != null) {
            cnsl_Reg.setCnslDt(cnslModiReqDto.getCnsl_date());
        }

        if (cnslModiReqDto.getCnsl_start_time() != null) {
            cnsl_Reg.setCnslStartTime(cnslModiReqDto.getCnsl_start_time());
        }

        if (cnslModiReqDto.getCnsl_title() != null) {
            cnsl_Reg.setCnslTitle(cnslModiReqDto.getCnsl_title());
        }

        if (cnslModiReqDto.getCnsl_content() != null) {
            cnsl_Reg.setCnslContent(cnslModiReqDto.getCnsl_content());
        }

        // 민감 키워드 자동 검사 (상담 수정: 제목/내용)
        try {
            String fullContent = (cnsl_Reg.getCnslTitle() != null ? cnsl_Reg.getCnslTitle() : "") + " " +
                    (cnsl_Reg.getCnslContent() != null ? cnsl_Reg.getCnslContent() : "");
            List<java.util.Map<String, Object>> detected = keywordService.detectSensitiveKeywords(fullContent);
            if (!detected.isEmpty()) {
                keywordService.recordRiskPost(
                        "cnsl_reg_update",
                        "CNSL",
                        cnsl_Reg.getCnslId(),
                        fullContent,
                        detected,
                        cnsl_Reg.getMemberId() != null ? cnsl_Reg.getMemberId().getMemberId() : null
                );
                log.warn("⚠️ 민감 키워드 감지된 상담 수정: cnslId={}, 키워드 개수={}", cnsl_Reg.getCnslId(), detected.size());
            }
        } catch (Exception e) {
            log.warn("민감 키워드 상담 수정 감지/기록 실패: cnslId={}, err={}", cnslId, e.getMessage());
        }

        return cnslId;
    }

    // [상담 취소]
    @Transactional
    public void removeMyCounseling(Long cnslId) {
        Cnsl_Reg cnsl_Reg = cnslRepository.findById(cnslId)
                .orElseThrow(() -> new IllegalArgumentException("예약된 상담이 없습니다."));
        // !cnsl_Reg.getMemberId().equals(현재 로그인한 member_id) throw new AccesException ~
        // [상담 취소 가능 여부]
        if (!"A".equals(cnsl_Reg.getCnslStat()) && !"B".equals(cnsl_Reg.getCnslStat()))
            throw new IllegalStateException("삭제 불가능한 상담 상태입니다.");
        // [취소 되어 있는지 확인]
        if ("Y".equals(cnsl_Reg.getDelYn()))
            throw new IllegalStateException("이미 취소된 상담입니다.");
        // [취소 가능 시간 제한]
        LocalDate counselStartAt = cnsl_Reg.getCnslDt();
        LocalDate cancelDeadline = counselStartAt.minusDays(1);
        LocalDate now = LocalDate.now();
        if (now.isAfter(cancelDeadline))
            throw new IllegalStateException("상담 시작 1일 전까지만 취소 가능합니다.");

        // [해당 상담사의 상담 금액 get]
        Long cnslPrice = cnslInfoRepository.findCnslPrice(cnsl_Reg.getCnslerId().getMemberId(), cnsl_Reg.getCnslTp());

        // [현재 잔액 get]
        Wallet wallet = walletRepository.findByEmail(cnsl_Reg.getMemberId().getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("지갑 정보가 없습니다."));
        Long currPoint = wallet.getCurrPoint();

        /*
         * [환불 정책]
         * - 작일 취소 70% 환불
         * - 당일 취소 50% 환불
         */

        // [작일 취소]
        if (now.equals(cancelDeadline)) {
            cnslPrice = cnslPrice * 70 / 100;
        }
        // [당일 취소]
        else if (now.equals(counselStartAt)) {
            cnslPrice = cnslPrice * 50 / 100;
        }

        // [포인트 내역 생성]
        PointHistory pointHistory = PointHistory
                .builder()
                .memberId(cnsl_Reg.getMemberId())
                .amount(cnslPrice)
                .pointAfter(currPoint + cnslPrice)
                .cnslId(cnsl_Reg.getCnslId())
                .brief(cnsl_Reg.getCnslerId().getNickname() + "상담 취소")
                .build();

        pointHistoryRepository.save(pointHistory);

        // [포인트 지갑 입력]
        wallet.setCurrPoint(currPoint + cnslPrice);
        walletRepository.save(wallet);

        cnsl_Reg.setDelYn("Y");
        cnsl_Reg.setCnslStat("X");
        cnslRepository.save(cnsl_Reg);
    }

    // [상담사 리스트] 진입=가격만 / 상담방식만 선택=getCounselorListByMethod / 상담유형 선택=getCounselorListWithFilters / 해시태그 선택=getCounselorListByHashTags
    public Page<CounselorListDto> getCounselorList(Pageable pageable, CounselorListReqeustDto requestDto) {
        Integer minPrice = requestDto.getMinPrice();
        Integer maxPrice = requestDto.getMaxPrice();
        List<String> cnslCate = requestDto.getCnslCate();
        if (cnslCate != null && cnslCate.isEmpty()) cnslCate = null;
        List<String> cnslTp = requestDto.getCnslTp();
        if (cnslTp != null && cnslTp.isEmpty()) cnslTp = null;
        String[] hashTags = requestDto.getHashTags();
        if (hashTags != null && (hashTags.length == 0 || (hashTags.length == 1 && (hashTags[0] == null || hashTags[0].isBlank())))) {
            hashTags = null;
        }
        if (hashTags != null) {
            List<String> cate = cnslCate != null ? cnslCate : List.of("1", "2", "3");
            List<String> tp = cnslTp != null ? cnslTp : List.of("1", "2", "3", "4", "5", "6");
            return cnslRepository.getCounselorListByHashTags(pageable, cate, tp, minPrice, maxPrice, hashTags);
        }
        if (cnslTp != null && (cnslCate == null || cnslCate.isEmpty())) {
            return cnslRepository.getCounselorListByMethod(pageable, cnslTp, minPrice, maxPrice);
        }
        if (cnslCate != null) {
            List<String> tp = cnslTp != null ? cnslTp : List.of("1", "2", "3", "4", "5", "6");
            return cnslRepository.getCounselorListWithFilters(pageable, cnslCate, tp, minPrice, maxPrice);
        }
        return cnslRepository.getCounselorList(pageable, minPrice, maxPrice);
    }
    
    // [상담사 뷰]
	public CounselorListDto getCounselor(String memberId) {
		return cnslRepository.getCounselor(memberId);
	}


    // =========== SYSTEM ===========
    // [기간 내 상담 건수 : 상담 상태별]
    public ConsultationStatusCountDto findConsultationStatusCounts(String cnslerId, LocalDate startDate,
            LocalDate endDate) {
        return cnslRepository.findConsultationStatusCounts(cnslerId, startDate, endDate);
    }

    // [기간 내 상담 건수 : 카테고리별]
    public List<ConsultationCategoryCountDto> findConsultationCategoryCounts(String cnslerId, LocalDate startDate,
            LocalDate endDate) {
        return cnslRepository.findConsultationCategoryCounts(cnslerId, startDate, endDate);
    }

    // [일자별 예약 및 완료 건수 추이]
    public List<ConsultationStatusDailyDto> findDailyReservationCompletionTrend(String cnslerId, LocalDate startDate,
            LocalDate endDate) {
        return cnslRepository.findDailyReservationCompletionTrend(cnslerId, startDate, endDate);
    }

    // [선택 기간 내 수익, 최근 3달 수익]
    public List<MyRevenueSummaryDto> findMyRevenueSummary(String cnslerId, LocalDate startDate, LocalDate endDate) {
        return cnslRepository.findMyRevenueSummary(cnslerId, startDate, endDate);
    }

    // [가장 많은 상담 유형]
    public MostConsultedTypeDto findMostConsultedType(String cnslerId, LocalDate startDate, LocalDate endDate) {
        return cnslRepository.findMostConsultedType(cnslerId, startDate, endDate);
    }

    // [상담 뷰]
    public CounselDetailDto getCounselDetail(Long cnslId, String email) {
        return cnslRepository.getCounselDetail(cnslId, email);
    }

//    // [상담사 월별 상담 건수]
//    public List<CnslDatePerMonthClassDto> findCounselingMonthlyCountByCounselor(String cnslerId) {
//        /*
//         * 1. 쿼리로 startMonth와 cnt를 가져온다.
//         * 2. 모든 것을 DB에 맡기는 것을 방지하기 위해 스프링에서 endMonth를 구한다.
//         * 3. 이때, 쿼리값을 가져오는 DTO는 interface기 때문에 class 형태의 Dto를 추가로 만들어 값을 저장한다.
//         * */
//        List<CnslDatePerMonthDto> results = cnslRepository.getCnslDatePerMonthList(cnslerId);
//
//        return results.stream().map(r -> {
//            LocalDate monthStart = r.getMonthStart();
//            LocalDate monthEnd = YearMonth.from(monthStart).atEndOfMonth();
//
//            return CnslDatePerMonthClassDto
//                    .builder()
//                    .monthStart(monthStart)
//                    .monthEnd(monthEnd)
//                    .totalCnt(r.getTotalCnt())
//                    .reservedCnt(r.getReservedCnt())
//                    .completedCnt(r.getCompletedCnt())
//                    .build();
//        }).toList();
//    }
//
//    // [상담사 전체 건수]
//    public Optional<CnslSumDto> findCounselingTotalCountByCounselor(String cnslerId) {
//        return cnslRepository.getCnslTotalCount(cnslerId);
//    }
//
    
    // [모든 상담 내역]
	public Page<cnslListDto> findAllCounselingsByCounselor(Pageable pageable, String cnslerId) {
		return cnslRepository.findAllCounselingsByCounselor(pageable, cnslerId);
	}
    // [상담 내역(전체) stat으로 나눔]
    public Page<cnslListDto> findCounselingsByCounselor(CounselingStatus status, Pageable pageable, String cnslerId) {
        String stat = status == null ? null : status.name();
        return cnslRepository.findCounselingsByCounselor(stat, pageable, cnslerId);
    }

    // [상담 예약 관리(수락 전)]
    public Page<cnslListWithoutStatusDto> findPendingReservations(Pageable pageable, String cnslerId) {
        return cnslRepository.findPendingReservations(pageable, cnslerId);
    }

    // [상담 수락]
    @Transactional
    public void approveConsultation(Long cnslId, String message) {
        Cnsl_Reg cnsl_Reg = cnslRepository.findById(cnslId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담입니다."));

        // [상담 상태에 따른 수락 가능 여부]
        if (!"A".equals(cnsl_Reg.getCnslStat()))
            throw new IllegalStateException("현재 상담 상태에서는 수락이 불가능합니다.");

        // [당일 수락 시 시간 제한 3시간]
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime consultationTime = LocalDateTime.of(
                cnsl_Reg.getCnslDt(),
                cnsl_Reg.getCnslStartTime());

        if (!now.isBefore(consultationTime.minusHours(3))) {
            throw new IllegalStateException("상담 시작 3시간 이내에는 수락이 불가능합니다.");
        }

        Cnsl_Resp cnsl_resp = Cnsl_Resp
                .builder()
                .cnslId(cnsl_Reg)
                .memberId(cnsl_Reg.getCnslerId())
                .content(message)
                .delYn("N")
                .build();

        cnsl_Reg.setCnslStat("B");
        cnslRepository.save(cnsl_Reg);
        cnslRespRepository.save(cnsl_resp);

        // 민감 키워드 자동 검사 (상담 내역 메시지)
        try {
            String fullContent = message != null ? message : "";
            List<java.util.Map<String, Object>> detected = keywordService.detectSensitiveKeywords(fullContent);
            if (!detected.isEmpty()) {
                keywordService.recordRiskPost(
                        "cnsl_resp",
                        "CNSL",
                        cnsl_Reg.getCnslId(),
                        fullContent,
                        detected,
                        cnsl_Reg.getCnslerId() != null ? cnsl_Reg.getCnslerId().getMemberId() : null
                );
                log.warn("⚠️ 민감 키워드 감지된 상담 승인 메시지: cnslId={}, 키워드 개수={}", cnsl_Reg.getCnslId(), detected.size());
            }
        } catch (Exception e) {
            log.warn("민감 키워드 상담 승인 메시지 감지/기록 실패: cnslId={}, err={}", cnslId, e.getMessage());
        }
    }

    // [상담 거절]
    @Transactional
    public void rejectConsultation(Long cnslId, String message) {
        Cnsl_Reg cnsl_Reg = cnslRepository.findById(cnslId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담입니다."));

        // [상담 상태에 따른 거절 가능 여부]
        if (!"A".equals(cnsl_Reg.getCnslStat()))
            throw new IllegalStateException("현재 상담 상태에서는 거절이 불가능합니다.");

        // [당일 거절 시 시간 제한 3시간]
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime consultationTime = LocalDateTime.of(
                cnsl_Reg.getCnslDt(),
                cnsl_Reg.getCnslStartTime());

        if (!now.isBefore(consultationTime.minusHours(3))) {
            throw new IllegalStateException("상담 시작 3시간 이내에는 거절이 불가능합니다.");
        }

        Cnsl_Resp cnsl_resp = Cnsl_Resp
                .builder()
                .cnslId(cnsl_Reg)
                .memberId(cnsl_Reg.getCnslerId())
                .content(message)
                .delYn("N")
                .build();

        cnsl_Reg.setCnslStat("X");
        cnsl_Reg.setCnslTodoYn("N");
        cnslRepository.save(cnsl_Reg);
        cnslRespRepository.save(cnsl_resp);

        // 민감 키워드 자동 검사 (상담 내역 메시지)
        try {
            String fullContent = message != null ? message : "";
            List<java.util.Map<String, Object>> detected = keywordService.detectSensitiveKeywords(fullContent);
            if (!detected.isEmpty()) {
                keywordService.recordRiskPost(
                        "cnsl_resp",
                        "CNSL",
                        cnsl_Reg.getCnslId(),
                        fullContent,
                        detected,
                        cnsl_Reg.getCnslerId() != null ? cnsl_Reg.getCnslerId().getMemberId() : null
                );
                log.warn("⚠️ 민감 키워드 감지된 상담 거절 메시지: cnslId={}, 키워드 개수={}", cnsl_Reg.getCnslId(), detected.size());
            }
        } catch (Exception e) {
            log.warn("민감 키워드 상담 거절 메시지 감지/기록 실패: cnslId={}, err={}", cnslId, e.getMessage());
        }
    }

    // =========== ADMIN ===========
    // [기간 내 상담 건수 및 수익 : 카테고리별]
    public List<CategoryRevenueStatisticsDto> findCategoryRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        return cnslRepository.findCategoryRevenueStatistics(startDate, endDate);
    }

    // [기간 내 상담 건수 및 수익 : 상담 유형별]
    public List<CategoryRevenueStatisticsDto> findTypeRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        return cnslRepository.findTypeRevenueStatistics(startDate, endDate);
    }

    // [실시간 위험 감지 및 조치 현황]
    public List<RealtimeRiskDetectionStatusDto> findRealtimeRiskDetectionStatus() {
        return cnslRepository.findRealtimeRiskDetectionStatus();
    }

    // [정산현황 : 일자별전체 상담사 내역 관련 집계 (최근일, 상담매출액순)]
    public Page<CounselorRevenueLatestlyDto> findLatestlyCounselorRevenue(Pageable pageable) {
        return cnslRepository.findLatestlyCounselorRevenue(pageable);
    }

	public CnslDetailDto getMyCnslDetail(Integer cnslId, String memberId) {
		// TODO Auto-generated method stub
		return null;
	}

	// 마이페이지 상담내역 상담사 리스트
	public Page<MyCnslListDto> findmycnsllist(String memberId, Pageable pageable, String cnslTp) {
		if ("3".equals(cnslTp)) {
			return cnslRepository.findmycnsllistAi(memberId, pageable);
		}
		if ("counselor".equals(cnslTp) || "not3".equals(cnslTp)) {
			return cnslRepository.findmycnsllistCounselor(memberId, pageable);
		}
		return cnslRepository.findmycnsllist(memberId, pageable);
	}

	// 마이페이지 상담내역 상세 내용
	public Optional<CnslDetailDto> findcnslDetail(Long cnslId, String memberId) {
		return cnslRepository.findcnslDetail(cnslId, memberId);
	}

//	@Transactional
//	public ChatDto saveMessage(ChatDto chatDto, String memberId) {
//		// 1. 사용자 조회
//	    Member send = memberRepository.findByMemberId(memberId)
//	            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
//	    
//	    // 2. DTO -> Entity 변환 및 빌드
//	    Chat_Msg chatMsg = Chat_Msg.builder()
//	            .cnslId(chatDto.getCnslId())
//	            .content(chatDto.getContent())
//	            .role(chatDto.getRole())
//	            .memberId("USER".equals(chatDto.getRole()) ? send : null)
//	            .cnslerId("COUNSELOR".equals(chatDto.getRole()) ? send : null)
//	            .build();
//	    
//	    // 3. 저장 (Entity를 저장하고 저장된 Entity를 받음)
//	    Chat_Msg savedEntity = cnslRepository.save(chatMsg);
//	    
//	    // 4. Entity -> DTO 변환 (Controller에 전달하기 위함)
//	    return ChatDto.builder()
//	            .chatId(savedEntity.getChatId())
//	            .cnslId(savedEntity.getCnslId())
//	            .content(savedEntity.getContent())
//	            .role(savedEntity.getRole())
//	            .created_at(savedEntity.getCreated_at()) // 시간 정보 포함
//	            .build();
//	}
}
