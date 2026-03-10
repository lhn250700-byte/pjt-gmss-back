package com.study.spring.center.controller;

import com.study.spring.center.dto.KakaoPlaceDto;
import com.study.spring.center.entity.SupportCenter;
import com.study.spring.center.service.KakaoLocalService;
import com.study.spring.center.service.SupportCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/centers")
@Tag(name = "상담센터 위치", description = "취업지원/상담센터 목록 및 상세 API")
@RequiredArgsConstructor
@Log4j2
public class CenterController {

    private final SupportCenterService supportCenterService;
    private final KakaoLocalService kakaoLocalService;

    private static Map<String, Object> emptyCentersResult() {
        return Map.of(
                "centers", List.of(),
                "totalCount", 0L,
                "totalPages", 1
        );
    }

    @GetMapping
    @Operation(summary = "센터 목록", description = "검색·페이징·위치 기반 거리 정렬·반경(km) 필터")
    public ResponseEntity<Map<String, Object>> getCenters(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "7") int pageSize,
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lng", required = false) Double lng,
            @RequestParam(name = "radiusKm", required = false) Double radiusKm) {
        try {
            Map<String, Object> result = supportCenterService.getCenters(query, page, pageSize, lat, lng, radiusKm);
            return ResponseEntity.ok(result);
        } catch (Throwable t) {
            log.error("getCenters failed (query={}, page={})", query, page, t);
            return ResponseEntity.ok(emptyCentersResult());
        }
    }

    /** 반경 검색은 /{id} 보다 먼저 선언 (경로 충돌 방지) */
    @GetMapping("/kakao-nearby")
    @Operation(summary = "카카오 로컬 반경 검색", description = "현재 위치 기준 반경(km) 내 상담센터/복지센터/고용센터 등 검색")
    public ResponseEntity<Map<String, Object>> getKakaoNearby(
            @RequestParam(name = "lat") double lat,
            @RequestParam(name = "lng") double lng,
            @RequestParam(name = "radiusKm", defaultValue = "5") double radiusKm) {
        List<KakaoPlaceDto> places = kakaoLocalService.searchNearby(lat, lng, radiusKm);
        return ResponseEntity.ok(Map.of(
                "places", places,
                "totalCount", places.size(),
                "currentLocation", Map.of("lat", lat, "lng", lng),
                "radiusKm", radiusKm
        ));
    }

    /** 키워드 검색: /search/keyword 로 두어 /{id} 와 충돌 방지 */
    @GetMapping("/search/keyword")
    @Operation(summary = "카카오 로컬 키워드 검색", description = "사용자 입력 키워드로 상담센터/장소 검색 (위치·반경 선택)")
    public ResponseEntity<Map<String, Object>> getKakaoKeyword(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lng", required = false) Double lng,
            @RequestParam(name = "radiusKm", required = false) Double radiusKm) {
        List<KakaoPlaceDto> places = kakaoLocalService.searchByKeyword(query, lat, lng, radiusKm);
        return ResponseEntity.ok(Map.of(
                "places", places,
                "totalCount", places.size(),
                "query", query != null ? query : ""
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "센터 상세", description = "단일 센터 상세 정보")
    public ResponseEntity<SupportCenter> getCenter(@PathVariable("id") Long id) {
        return supportCenterService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
