package com.study.spring.Member.service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.study.spring.util.JWTUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlackListService {
	private final StringRedisTemplate redisTemplate;
	private static final String BLACKLIST_PREFIX = "blacklist:AT:";
    private static final String RT_PREFIX        = "auth:RT:";
    
    public void addToBlackList(String accessToken) {
    		try {
    			Map<String, Object> claims = JWTUtil.validateToken(accessToken);
    	        long exp = ((Number) claims.get("exp")).longValue();
    			
    	        long remainTtl = (exp * 1000) - System.currentTimeMillis();
    			if (remainTtl <= 0) {
    				log.info("AT가 이미 만료됨 -> 블랙리스트 등록 불필요");
    				return;
    			}
    			
    			String key = BLACKLIST_PREFIX + accessToken;
    			redisTemplate.opsForValue().set(key, "logout", remainTtl, TimeUnit.MILLISECONDS);
    			log.info("AT 블랙리스트 등록 완료, TTL={}ms", remainTtl);
    			
    		} catch (Exception e) {
    			log.warn("AT 블랙리스트 등록 중 오류 : {}", e.getMessage());
    		}
    }
    
    public boolean isBlack(String accessToken) {
    		return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
    
    public void saveRT(String email, String refreshToken) {
    		redisTemplate.opsForValue().set(RT_PREFIX + email, refreshToken, 24, TimeUnit.HOURS);
    		log.info("RT 저장 완료: email={}", email);
    }
    
    public boolean isRefreshTokenValid(String email, String refreshToken) {
    		String stored = redisTemplate.opsForValue().get(RT_PREFIX + email);
    		return refreshToken.equals(stored);
    }
    
    public void deleteRefreshToken(String email) {
    		redisTemplate.delete(RT_PREFIX + email);
    		log.info("RT 삭제 완료: email={}", email);
    }
    
    public void rTT(String email, String newRefreshToken) {
    		saveRT(email, newRefreshToken);
    		log.info("RT 로테이션 완료: email={}", email);
    }
}
