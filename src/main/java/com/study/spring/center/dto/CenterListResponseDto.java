package com.study.spring.center.dto;

import com.study.spring.center.entity.SupportCenter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CenterListResponseDto {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private String businessHours;
    private String description;
    private String website;
    private String category;

    public static CenterListResponseDto fromEntity(SupportCenter c, Double distanceKm) {
        return CenterListResponseDto.builder()
                .id(c.getId())
                .name(c.getName())
                .address(c.getAddress())
                .phone(c.getPhone())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .distanceKm(distanceKm != null ? Math.round(distanceKm * 10.0) / 10.0 : null)
                .businessHours(c.getBusinessHours())
                .description(c.getDescription())
                .website(c.getWebsite())
                .category(c.getCategory())
                .build();
    }
}
