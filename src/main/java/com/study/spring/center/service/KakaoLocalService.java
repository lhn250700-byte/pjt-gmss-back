package com.study.spring.center.service;

import com.study.spring.center.dto.KakaoKeywordResponse;
import com.study.spring.center.dto.KakaoPlaceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 카카오 로컬 API - 키워드로 장소 검색 (반경 5km 내 상담센터/복지센터 등)
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class KakaoLocalService {

    private static final String KEYWORD_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";
    private static final int RADIUS_METERS_5KM = 5000;
    private static final String[] SEARCH_KEYWORDS = {
            "상담센터", "복지센터", "고용센터", "취업지원센터", "청년센터", "고용노동부", "취업상담"
    };

    @Value("${kakao.rest-api-key:}")
    private String restApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 반경(km) 내 키워드 검색 - 여러 키워드로 검색 후 병합·중복 제거·거리순 정렬
     * 예외 시 빈 목록 반환 (500 방지)
     */
    public List<KakaoPlaceDto> searchNearby(double lat, double lng, double radiusKm) {
        try {
            if (restApiKey == null || restApiKey.isBlank()) {
                log.warn("kakao.rest-api-key not set");
                return List.of();
            }
            int radiusMeters = (int) Math.min(20000, Math.round(radiusKm * 1000));
            String x = String.valueOf(lng);
            String y = String.valueOf(lat);

            Set<String> seenIds = new HashSet<>();
            List<KakaoPlaceDto> all = new ArrayList<>();

            for (String keyword : SEARCH_KEYWORDS) {
                try {
                String url = UriComponentsBuilder.fromUriString(KEYWORD_SEARCH_URL)
                        .queryParam("query", keyword)
                        .queryParam("x", x)
                        .queryParam("y", y)
                        .queryParam("radius", radiusMeters)
                        .queryParam("size", 15)
                        .queryParam("sort", "distance")
                        .build()
                        .toUriString();

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "KakaoAK " + restApiKey);
                ResponseEntity<KakaoKeywordResponse> res = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        KakaoKeywordResponse.class
                );

                if (res.getBody() == null || res.getBody().getDocuments() == null) continue;

                for (KakaoKeywordResponse.Document doc : res.getBody().getDocuments()) {
                    if (doc.getId() == null || seenIds.contains(doc.getId())) continue;
                    seenIds.add(doc.getId());

                    double latDoc = parseDouble(doc.getY(), 0);
                    double lngDoc = parseDouble(doc.getX(), 0);
                    double distanceKmVal = doc.getDistance() != null && !doc.getDistance().isEmpty()
                            ? parseDouble(doc.getDistance(), 0) / 1000.0
                            : haversine(lat, lng, latDoc, lngDoc);

                    String address = doc.getRoadAddressName() != null && !doc.getRoadAddressName().isBlank()
                            ? doc.getRoadAddressName()
                            : doc.getAddressName();

                    all.add(KakaoPlaceDto.builder()
                            .id(doc.getId())
                            .name(doc.getPlaceName())
                            .address(address)
                            .phone(doc.getPhone())
                            .latitude(latDoc)
                            .longitude(lngDoc)
                            .distanceKm(Math.round(distanceKmVal * 10.0) / 10.0)
                            .categoryName(doc.getCategoryName())
                            .placeUrl(doc.getPlaceUrl())
                            .source("kakao")
                            .build());
                }
                } catch (Exception e) {
                    log.warn("Kakao keyword search failed for '{}': {}", keyword, e.getMessage());
                }
            }

            return all.stream()
                    .sorted(Comparator.comparing(KakaoPlaceDto::getDistanceKm, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Kakao searchNearby failed", e);
            return List.of();
        }
    }

    /**
     * 사용자 입력 키워드로 카카오 로컬 API 키워드 검색 (상담센터 위치 검색용)
     * - query가 비어 있으면 빈 목록 반환
     * - lat/lng/radiusKm 이 있으면 해당 반경 내만, 없으면 전국 검색(카카오 기본)
     */
    public List<KakaoPlaceDto> searchByKeyword(String query, Double lat, Double lng, Double radiusKm) {
        try {
            if (restApiKey == null || restApiKey.isBlank()) {
                log.warn("kakao.rest-api-key not set");
                return List.of();
            }
            String q = (query != null) ? query.trim() : "";
            if (q.isEmpty()) return List.of();

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYWORD_SEARCH_URL)
                    .queryParam("query", q)
                    .queryParam("size", 15)
                    .queryParam("sort", "distance");
            if (lat != null && lng != null) {
                builder.queryParam("x", String.valueOf(lng));
                builder.queryParam("y", String.valueOf(lat));
                int radiusMeters = (radiusKm != null && radiusKm > 0)
                        ? (int) Math.min(20000, Math.round(radiusKm * 1000))
                        : RADIUS_METERS_5KM;
                builder.queryParam("radius", radiusMeters);
            }
            String url = builder.build().toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + restApiKey);
            ResponseEntity<KakaoKeywordResponse> res = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    KakaoKeywordResponse.class
            );

            if (res.getBody() == null || res.getBody().getDocuments() == null)
                return List.of();

            double latVal = (lat != null) ? lat : 0;
            double lngVal = (lng != null) ? lng : 0;
            List<KakaoPlaceDto> list = new ArrayList<>();
            for (KakaoKeywordResponse.Document doc : res.getBody().getDocuments()) {
                if (doc.getId() == null) continue;
                double latDoc = parseDouble(doc.getY(), 0);
                double lngDoc = parseDouble(doc.getX(), 0);
                double distanceKmVal = (lat != null && lng != null && doc.getDistance() != null && !doc.getDistance().isEmpty())
                        ? parseDouble(doc.getDistance(), 0) / 1000.0
                        : haversine(latVal, lngVal, latDoc, lngDoc);
                String address = (doc.getRoadAddressName() != null && !doc.getRoadAddressName().isBlank())
                        ? doc.getRoadAddressName()
                        : doc.getAddressName();
                list.add(KakaoPlaceDto.builder()
                        .id(doc.getId())
                        .name(doc.getPlaceName())
                        .address(address)
                        .phone(doc.getPhone())
                        .latitude(latDoc)
                        .longitude(lngDoc)
                        .distanceKm(Math.round(distanceKmVal * 10.0) / 10.0)
                        .categoryName(doc.getCategoryName())
                        .placeUrl(doc.getPlaceUrl())
                        .source("kakao")
                        .build());
            }
            return list;
        } catch (Exception e) {
            log.error("Kakao searchByKeyword failed (query={})", query, e);
            return List.of();
        }
    }

    private static double parseDouble(String s, double def) {
        if (s == null || s.isBlank()) return def;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
