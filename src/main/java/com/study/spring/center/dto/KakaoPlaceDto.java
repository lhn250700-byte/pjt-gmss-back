package com.study.spring.center.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 프론트에 내려줄 카카오 장소 한 건 (거리 km 포함)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoPlaceDto {
    private String id;
    private String name;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private String categoryName;
    private String placeUrl;
    /** 프론트 구분용: 'kakao' */
    private String source;
}
