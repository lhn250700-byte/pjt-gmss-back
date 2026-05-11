package com.study.spring.Bbs.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.study.spring.Bbs.service.BbsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BbsScheduler {

    private final BbsService bbsService;

    @Scheduled(cron = "0 0/10 * * * *")  // 10분마다
    public void refreshWeeklyPopularPosts() {
        log.info("[Scheduler] 주간 인기글 캐시 갱신 시작");
        try {
            bbsService.refreshWeeklyPopularPostsCache();
            log.info("[Scheduler] 주간 인기글 캐시 갱신 완료");
        } catch (Exception e) {
            log.error("[Scheduler] 주간 인기글 캐시 갱신 실패", e);
        }
    }
}