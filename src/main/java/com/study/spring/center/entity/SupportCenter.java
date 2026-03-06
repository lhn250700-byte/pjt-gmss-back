package com.study.spring.center.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "support_center")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;
    private String phone;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "business_hours")
    private String businessHours;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String website;

    /**
     * 센터 유형: government, youth, welfare, support 등
     */
    private String category;
}
