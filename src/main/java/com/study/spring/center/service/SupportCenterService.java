package com.study.spring.center.service;

import com.study.spring.center.dto.CenterListResponseDto;
import com.study.spring.center.entity.SupportCenter;
import com.study.spring.center.repository.SupportCenterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class SupportCenterService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final SupportCenterRepository supportCenterRepository;

    /**
     * 센터 목록 조회 (검색, 페이징, 선택적 거리 계산, 선택적 반경 필터)
     * - radiusKm 가 있으면 lat/lng 기준 반경 이내만 반환 (가까운 순)
     */
    public Map<String, Object> getCenters(String query, int page, int pageSize, Double lat, Double lng, Double radiusKm) {
        try {
            return getCentersInternal(query, page, pageSize, lat, lng, radiusKm);
        } catch (Exception e) {
            log.error("getCenters failed", e);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("centers", List.<CenterListResponseDto>of());
            fallback.put("totalCount", 0L);
            fallback.put("totalPages", 1);
            return fallback;
        }
    }

    private Map<String, Object> getCentersInternal(String query, int page, int pageSize, Double lat, Double lng, Double radiusKm) {
        int safePageSize = Math.min(100, Math.max(1, pageSize));
        int pageIndex = Math.max(0, page - 1);
        String searchQuery = (query != null && !query.isBlank()) ? query.trim() : null;
        Double userLat = (lat != null && lng != null) ? lat : null;
        Double userLng = (lat != null && lng != null) ? lng : null;
        boolean useRadius = (userLat != null && userLng != null && radiusKm != null && radiusKm > 0);

        List<SupportCenter> list;
        long totalCount;
        int totalPages;

        if (useRadius) {
            // 반경 모드: 전체 또는 검색 결과를 가져와 거리 필터 후 메모리 페이징
            List<SupportCenter> all = (searchQuery == null || searchQuery.isEmpty())
                    ? supportCenterRepository.findAll()
                    : supportCenterRepository.findByNameContainingIgnoreCase(searchQuery, PageRequest.of(0, 1000)).getContent();
            double radiusKmVal = radiusKm != null ? radiusKm : 0;
            double uLat = userLat != null ? userLat : 0;
            double uLng = userLng != null ? userLng : 0;
            List<CenterListResponseDto> withDistance = all.stream()
                    .map(c -> {
                        double cLat = c.getLatitude() != null ? c.getLatitude() : 0;
                        double cLng = c.getLongitude() != null ? c.getLongitude() : 0;
                        double km = haversine(uLat, uLng, cLat, cLng);
                        return CenterListResponseDto.fromEntity(c, km);
                    })
                    .filter(c -> c.getDistanceKm() != null && c.getDistanceKm() <= radiusKmVal)
                    .sorted(Comparator.comparing(CenterListResponseDto::getDistanceKm))
                    .collect(Collectors.toList());
            totalCount = withDistance.size();
            totalPages = (int) Math.max(1, Math.ceil((double) totalCount / safePageSize));
            int from = pageIndex * safePageSize;
            int to = Math.min(from + safePageSize, withDistance.size());
            List<CenterListResponseDto> pagedCenters = from < withDistance.size()
                    ? withDistance.subList(from, to)
                    : List.of();
            Map<String, Object> result = new HashMap<>();
            result.put("centers", pagedCenters);
            result.put("totalCount", totalCount);
            result.put("totalPages", totalPages);
            result.put("radiusKm", radiusKm);
            if (userLat != null && userLng != null) {
                result.put("currentLocation", Map.of("lat", userLat, "lng", userLng));
            }
            return result;
        }

        // 기존 페이징 모드
        Pageable pageable = PageRequest.of(pageIndex, safePageSize);
        Page<SupportCenter> pageResult = (searchQuery == null || searchQuery.isEmpty())
                ? supportCenterRepository.findAll(pageable)
                : supportCenterRepository.findByNameContainingIgnoreCase(searchQuery, pageable);
        list = pageResult.getContent();
        totalCount = pageResult.getTotalElements();
        totalPages = pageResult.getTotalPages();

        List<CenterListResponseDto> centers = list.stream()
                .map(c -> {
                    Double distance = null;
                    if (userLat != null && userLng != null && c.getLatitude() != null && c.getLongitude() != null) {
                        distance = haversine(userLat, userLng, c.getLatitude(), c.getLongitude());
                    }
                    return CenterListResponseDto.fromEntity(c, distance);
                })
                .collect(Collectors.toList());

        if (userLat != null && userLng != null) {
            centers.sort(Comparator.comparing(CenterListResponseDto::getDistanceKm,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("centers", centers);
        result.put("totalCount", totalCount);
        result.put("totalPages", totalPages);
        if (userLat != null && userLng != null) {
            result.put("currentLocation", Map.of("lat", userLat, "lng", userLng));
        }
        return result;
    }

    public Optional<SupportCenter> getById(Long id) {
        return supportCenterRepository.findById(id);
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
