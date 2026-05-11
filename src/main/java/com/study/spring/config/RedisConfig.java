package com.study.spring.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {

	@Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // LocalDateTime 지원
        mapper.registerModule(new JavaTimeModule());

        // timestamp 형태 방지 (ISO 8601)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }

	@Bean
	public RedisCacheManager redisCacheManager(
	        RedisConnectionFactory connectionFactory,
	        @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {

	    GenericJackson2JsonRedisSerializer serializer =
	            new GenericJackson2JsonRedisSerializer(redisObjectMapper);

	    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
	            .entryTtl(Duration.ofMinutes(15))
	            .serializeKeysWith(
	                    RedisSerializationContext.SerializationPair
	                            .fromSerializer(new StringRedisSerializer())
	            )
	            .serializeValuesWith(
	                    RedisSerializationContext.SerializationPair
	                            .fromSerializer(serializer)
	            );

	    return RedisCacheManager.builder(connectionFactory)
	            .cacheDefaults(config)
	            .build();
	}
	
	@Bean
    @Primary // 기본적으로 이 템플릿을 사용하도록 설정
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Key 직렬화: String (토큰이나 이메일이 들어감)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        
        // Value 직렬화: JSON (나중에 객체를 저장할 수도 있으므로)
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        redisTemplate.setValueSerializer(serializer);
        
        // Hash 구조를 쓸 경우를 대비한 설정
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        return redisTemplate;
    }
}