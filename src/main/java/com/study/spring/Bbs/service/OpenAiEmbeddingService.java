package com.study.spring.Bbs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * OpenAI Embeddings API를 사용한 임베딩 서비스.
 * text-embedding-3-small (1536차원) 사용.
 * app.embedding.openai-api-key 가 설정된 경우에만 빈으로 등록됩니다.
 */
@Service
@Primary
@ConditionalOnProperty(name = "app.embedding.openai-api-key")
@Log4j2
public class OpenAiEmbeddingService implements EmbeddingService {

    private static final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";
    private static final String MODEL = "text-embedding-3-small";
    private static final int DIMENSIONS = 1536;

    private final String apiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiEmbeddingService(
            @Value("${app.embedding.openai-api-key:}") String apiKey) {
        this.apiKey = (apiKey != null && !apiKey.isBlank()) ? apiKey.trim() : null;
    }

    @Override
    public float[] embed(String text) {
        if (apiKey == null || text == null || text.isBlank()) {
            return null;
        }
        try {
            String payload = String.format(
                    "{\"model\":\"%s\",\"input\":%s}",
                    MODEL,
                    objectMapper.writeValueAsString(text)
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    EMBEDDING_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.path("data");
                if (data.isArray() && data.size() > 0) {
                    JsonNode emb = data.get(0).path("embedding");
                    if (emb.isArray()) {
                        float[] out = new float[Math.min(emb.size(), DIMENSIONS)];
                        for (int i = 0; i < out.length; i++) {
                            out[i] = (float) emb.get(i).asDouble();
                        }
                        return out;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("OpenAI embedding 실패: {}", e.getMessage());
        }
        return null;
    }
}
