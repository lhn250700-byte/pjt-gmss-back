package com.study.spring.activity.service;

import com.study.spring.activity.entity.ActivityLog;
import com.study.spring.activity.repository.ActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
@Slf4j
@Service
public class ActivityLogger {

    private final ActivityLogRepository logRepository;

    /**
     * 활동 로그 기록
     * @param memberId 회원 ID
     * @param memberEmail 회원 이메일
     * @param memberRole 회원 역할
     * @param actionType 활동 타입 (create, update, delete 등)
     * @param targetType 대상 타입 (post, comment, review 등)
     * @param targetId 대상 ID
     * @param description 설명
     * @param request HTTP 요청 객체
     */
    public void logActivity(String memberId, String memberEmail, String memberRole,
                           String actionType, String targetType, Long targetId,
                           String description, HttpServletRequest request) {
        try {
            ActivityLog.ActivityLogBuilder logBuilder = ActivityLog.builder()
                    .memberId(memberId)
                    .memberEmail(memberEmail)
                    .memberRole(memberRole)
                    .actionType(actionType)
                    .targetType(targetType)
                    .targetId(targetId != null ? targetId.intValue() : null)
                    .description(description);
            
            if (request != null) {
                logBuilder
                    .ipAddress(getClientIP(request))
                    .userAgent(request.getHeader("User-Agent"));
            }
            
            ActivityLog activityLog = logBuilder.build();
            logRepository.save(activityLog);
            
            log.info("📝 활동 로그: {} {} {} (ID: {})", actionType, targetType, memberId, targetId);
        } catch (Exception e) {
            // 로깅 실패해도 메인 기능에 영향 없도록
            log.error("활동 로그 기록 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 간단한 로깅 메서드 (request 없이)
     */
    public void logActivity(String memberId, String actionType, String targetType, Long targetId, String description) {
        logActivity(memberId, null, "USER", actionType, targetType, targetId, description, null);
    }
    
    /**
     * 클라이언트 IP 추출 (프록시 고려)
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
