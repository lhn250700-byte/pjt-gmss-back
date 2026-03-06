package com.study.spring.center.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 카카오 로컬 API - 키워드로 장소 검색 응답
 * https://dapi.kakao.com/v2/local/search/keyword.json
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoKeywordResponse {

    private Meta meta;
    private List<Document> documents;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private Integer total_count;
        private Integer pageable_count;
        private Boolean is_end;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private String id;
        @JsonProperty("place_name")
        private String placeName;
        @JsonProperty("category_name")
        private String categoryName;
        private String phone;
        @JsonProperty("address_name")
        private String addressName;
        @JsonProperty("road_address_name")
        private String roadAddressName;
        private String x; // longitude
        private String y; // latitude
        @JsonProperty("place_url")
        private String placeUrl;
        private String distance; // meters (when x,y in request)
    }
}
