package com.study.spring.Cnsl.controller;

import java.lang.String;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.study.spring.Cnsl.entity.Chat_Msg;
import com.study.spring.Cnsl.entity.CounselingStatus;
import com.study.spring.Cnsl.dto.*;
import com.study.spring.Member.dto.MemberDto;
import org.apache.coyote.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.study.spring.Cnsl.repository.CnslRepository;
import com.study.spring.Cnsl.service.CnslService;

@RestController
public class CnslController {
	@Autowired
	CnslService cnslService;

	@PostMapping("/api/cnslReg_create")
	public ResponseEntity<?> createCounselingReservation(@RequestBody CnslReqDto cnslReqDto) {
		try {
			Long id = cnslService.reserveCounseling(cnslReqDto);
			return ResponseEntity.ok(id);
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Apply failure: " + e.getMessage());
		}
	}

	/** AI 즉시 상담 생성 (cnsl_tp=3). 마이페이지 목록·재진입에 사용 */
	@PostMapping("/api/cnslReg_createAi")
	public ResponseEntity<?> createAiCounseling(
			@AuthenticationPrincipal(expression = "username") String memberId) {
		if (memberId == null || memberId.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
		}
		try {
			Long id = cnslService.reserveCounselingAi(memberId.trim());
			return ResponseEntity.ok(id);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Apply failure: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
		}
	}

	/** 진행 중 AI 상담 ID (재진입 리다이렉트용) */
	@GetMapping("/api/mypage/activeAiCnsl")
	public ResponseEntity<?> getActiveAiCnsl(
			@AuthenticationPrincipal(expression = "username") String memberId) {
		if (memberId == null || memberId.isBlank()) {
			return ResponseEntity.ok(java.util.Map.of());
		}
		Long cnslId = cnslService.findActiveAiCnslId(memberId.trim());
		return ResponseEntity.ok(cnslId != null ? java.util.Map.of("cnslId", cnslId) : java.util.Map.of());
	}
	
	// 예약 벨리데이션 체크 [reserve 서비스 코드 내에 있긴 있음]
	@GetMapping("/api/cnslReg_reservationDuplicateChk")
	public Optional<IsCnslDto> isCounseling(
			@RequestParam("memberId") String memberId,
			@RequestParam("cnslerId") String cnslerId,
			@RequestParam("cnslDt") LocalDate cnslDt,
			@RequestParam("cnslStartTime") LocalTime cnslStartTime) {
		Optional<IsCnslDto> isCounseling =  cnslService.isCounseling(memberId, cnslerId, cnslDt, cnslStartTime);
		return isCounseling;
	}

	// 상담사의 특정 일자 예약 리스트
	@GetMapping("/api/cnslReg_availableTimeList")
	public List<CnslerDateDto> getAvailableSlots(
			@RequestParam("cnslerId") String cnslerId,
			@RequestParam("cnslDt") LocalDate cnslDt) {

		return cnslService.getAvailableSlotsForCnsler(cnslerId, cnslDt);
	}
	
	@PatchMapping("/api/cnslReg_update/{cnslId}")
	public ResponseEntity<?> updateMyCounseling(
            @PathVariable Long cnslId,
        @RequestBody CnslModiReqDto cnslModiReqDto
	) {
		try {
			Long id = cnslService.modifyMyCounseling(cnslId, cnslModiReqDto);
			return ResponseEntity.ok(id);
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("update failure: " + e.getMessage());
		}
	}
	
	@PutMapping("/api/cnslReg_cancel/{cnslId}")
	public ResponseEntity<?> cancelMyCounseling(@PathVariable Long cnslId) {
		try {
			cnslService.removeMyCounseling(cnslId);
			return ResponseEntity.ok("삭제 성공");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("delete failure: " + e.getMessage());
		}
		
	}

	// [상담사 찾기]
	// [상담사 리스트]
	@GetMapping("/api/counselorList")
	public ResponseEntity<Page<CounselorListDto>> getCounselorList(
		@RequestParam(name="page", defaultValue = "0") int page,
		@RequestParam(name="size", defaultValue = "10") int size,
		CounselorListReqeustDto requestDto
	) {
		Pageable pageable = PageRequest.of(page, size);
		Page<CounselorListDto> counselorList = cnslService.getCounselorList(pageable, requestDto);
		return ResponseEntity.status(200).body(counselorList);
	}
	
	// [상담사 뷰]
	@GetMapping("/api/counselor/{memberId}")
	public ResponseEntity<?> getCounselor(@PathVariable("memberId") String memberId) {
		try {
			return ResponseEntity.ok(cnslService.getCounselor(memberId));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("counselor view failure: " + e.getMessage());
		}
	}

	// =========== SYSTEM ===========
	// [기간 내 상담 건수 : 상담 상태별]
	@GetMapping("/api/cnslReg_statusStatistics")
	public ConsultationStatusCountDto getConsultationStatusCounts(@RequestParam("cnslerId") String cnslerId, @RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate) {
		return cnslService.findConsultationStatusCounts(cnslerId, startDate, endDate);
	}

	// [기간 내 상담 건수 : 카테고리별]
	@GetMapping("/api/cnslReg_categoryStatistics")
	public List<ConsultationCategoryCountDto> getConsultationCategoryCounts(@RequestParam("cnslerId") String cnslerId, @RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate) {
		return cnslService.findConsultationCategoryCounts(cnslerId, startDate, endDate);
	}

	// [일자별 예약 및 완료 건수 추이]
	@GetMapping("/api/cnslReg_dailyStatusStatistics")
	public List<ConsultationStatusDailyDto> getDailyReservationCompletionTrend(@RequestParam("cnslerId") String cnslerId, @RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate) {
		return cnslService.findDailyReservationCompletionTrend(cnslerId, startDate, endDate);
	}

	// [선택 기간 내 수익, 최근 3달 수익]
	@GetMapping("/api/cnslReg_revenueSummary")
	public List<MyRevenueSummaryDto> getMyRevenueSummary(@RequestParam("cnslerId") String cnslerId, @RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate) {
		return cnslService.findMyRevenueSummary(cnslerId, startDate, endDate);
	}

	// [가장 많은 상담 유형]
	@GetMapping("/api/cnslReg_topTypeStatistics")
	public MostConsultedTypeDto getMostConsultedType(@RequestParam("cnslerId") String cnslerId, @RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate) {
		return cnslService.findMostConsultedType(cnslerId, startDate, endDate);
	}

	// [상담 뷰]
	@GetMapping("/api/cnslReg_counsels/{cnslId}")
	public ResponseEntity<?> getCounselDetail(@PathVariable("cnslId") Long cnslId, @AuthenticationPrincipal MemberDto principal) {
		try {
			System.out.println("컨트롤러 진입");

			System.out.println("principal = " + principal);
			return ResponseEntity.ok(cnslService.getCounselDetail(cnslId, principal.getEmail()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("get counselDetail failure: " + e.getMessage());
		}
	}


//	// [상담사 월별 상담 건수]
//	@GetMapping("/api/counselSum/{cnslerId}/monthly")
//	public List<CnslDatePerMonthClassDto> getMyCounselingMonthlyCount(@PathVariable String cnslerId) {
//		return cnslService.findCounselingMonthlyCountByCounselor(cnslerId);
//	}
//
//	// [상담사 전체 건수]
//	@GetMapping("/api/counselSum/{cnslerId}")
//	public Optional<CnslSumDto> getMyCounselingTotalCount(@PathVariable String cnslerId) {
//		return cnslService.findCounselingTotalCountByCounselor(cnslerId);
//	}
//
	
	// [전체 상담 내역]
	@GetMapping("/api/cnslReg_allList/{cnslerId}")
	public ResponseEntity<Page<cnslListDto>> getMyCounselingAllList(
			@RequestParam(name="page", defaultValue = "0") int page,
			@RequestParam(name="size", defaultValue = "5") int size,
            @PathVariable("cnslerId") String cnslerId
	) {
		Pageable pageable = PageRequest.of(page, size);

		Page<cnslListDto> cnslPage = cnslService.findAllCounselingsByCounselor(pageable, cnslerId);
		if (cnslPage.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(cnslPage);
	}
	
	// [상담 상태에 따른 상담 내역(전체)]
	@GetMapping("/api/cnslReg_statusList/{cnslerId}")
	public ResponseEntity<Page<cnslListDto>> getMyCounselingList(
			@RequestParam(name="status", required = false) CounselingStatus status,
			@RequestParam(name="page", defaultValue = "0") int page,
			@RequestParam(name="size", defaultValue = "10") int size,
			@PathVariable("cnslerId") String cnslerId
	) {
		Pageable pageable = PageRequest.of(page, size);

		Page<cnslListDto> cnslPage = cnslService.findCounselingsByCounselor(status, pageable, cnslerId);
		if (cnslPage.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(cnslPage);
	}


	// [상담 예약 관리(수락 전)]
	@GetMapping("/api/cnslReg_pendingReservationList/{cnslerId}")
	public ResponseEntity<Page<cnslListWithoutStatusDto>> getPendingReservationList(
			@RequestParam(name="page", defaultValue = "0") int page,
			@RequestParam(name="size", defaultValue = "10") int size,
			@PathVariable("cnslerId") String cnslerId
	) {
		Pageable pageable = PageRequest.of(page, size);

		Page<cnslListWithoutStatusDto> rsvPage = cnslService.findPendingReservations(pageable, cnslerId);
		if (rsvPage.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(rsvPage);
	}

	// [상담 수락]
	@PostMapping("/api/cnslReg_approve/{cnslId}")
	public ResponseEntity<?> approveConsultation(@PathVariable("cnslId") Long cnslId, @RequestBody cnslRespMessageDto message) {
		try {
			cnslService.approveConsultation(cnslId, message.getMessage());
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Response failure: " + e.getMessage());
		}
	}

	// [상담 거절]
	@PostMapping("/api/cnslReg_reject/{cnslId}")
	public ResponseEntity<?> rejectConsultation(@PathVariable("cnslId") Long cnslId, @RequestBody cnslRespMessageDto message) {
		try {
			cnslService.rejectConsultation(cnslId, message.getMessage());
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Response failure: " + e.getMessage());
		}
	}

	// =========== ADMIN ===========
	// [기간 내 상담 건수 및 수익 : 카테고리별]
	@GetMapping("/api/cnslReg_categoryRevenueStatistics")
	public List<CategoryRevenueStatisticsDto> getCategoryRevenueStatistics(@RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate) {
		return cnslService.findCategoryRevenueStatistics(startDate, endDate);
	}

	// [기간 내 상담 건수 및 수익 : 상담 유형별]
	@GetMapping("/api/cnslReg_typeRevenueStatistics")
	public List<CategoryRevenueStatisticsDto> getTypeRevenueStatistics(@RequestParam("startDate") LocalDate startDate, @RequestParam("endDate") LocalDate endDate) {
		return cnslService.findTypeRevenueStatistics(startDate, endDate);
	}

	// [실시간 위험 감지 및 조치 현황]
	@GetMapping("/api/bbsRisk_realtimeList")
	public List<RealtimeRiskDetectionStatusDto> getRealtimeRiskDetectionStatus() {
		return cnslService.findRealtimeRiskDetectionStatus();
	}

	// [정산현황 : 일자별전체 상담사 내역 관련 집계 (최근일, 상담매출액순)]
	@GetMapping("/api/cnslReg_latestRevenue")
	public ResponseEntity<Page<CounselorRevenueLatestlyDto>> getLatestlyCounselorRevenue(
			@RequestParam(name="page", defaultValue = "0") int page,
			@RequestParam(name="size", defaultValue = "5") int size) {
		Pageable pageable = PageRequest.of(page, size);

		Page<CounselorRevenueLatestlyDto> revenuePage = cnslService.findLatestlyCounselorRevenue(pageable);
		if (revenuePage.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(revenuePage);
	}
	
	// 마이페이지 상담내역 (cnslTp: 3=AI상담만, counselor=상담사상담만, 미지정=전체)
	@GetMapping("/api/mypage/cnsllist")
	public ResponseEntity<Page<MyCnslListDto>> getmycnsllist(
			@AuthenticationPrincipal(expression = "username") String memberId,
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			@RequestParam(required = false) String cnslTp) {
		if (memberId == null || memberId.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		Page<MyCnslListDto> list = cnslService.findmycnsllist(memberId.trim(), pageable, cnslTp);
		return ResponseEntity.ok(list);
	}

	// 마이페이지 상담내역-상담사 상세
	@GetMapping("/api/mypage/cnsllist/{cnslId}")
	public ResponseEntity<CnslDetailDto> getcnslDetail(
			@AuthenticationPrincipal(expression = "username") String memberId,
			@PathVariable("cnslId") Long cnslId) {
		if (memberId == null || memberId.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return cnslService.findcnslDetail(cnslId, memberId.trim())
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	// 마이페이지 - 상담사와의 상담 채팅
//	@PostMapping("/api/mypage/cnslchat/{cnslId}")
//	public ResponseEntity<ChatDto> cnslChat(
//			@PathVariable("cnslId") Integer cnslId,
//			@AuthenticationPrincipal String memberId,
//			@RequestBody ChatDto chatDto){
//		chatDto.setCnslId(cnslId);
//		
//		ChatDto saveMsg = cnslService.saveMessage(chatDto, memberId);
//		
//		return ResponseEntity.ok(saveMsg);			
//	}
}
