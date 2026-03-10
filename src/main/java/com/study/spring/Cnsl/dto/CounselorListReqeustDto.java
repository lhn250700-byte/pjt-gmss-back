package com.study.spring.Cnsl.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CounselorListReqeustDto {
    List<String> cnslCate;
    List<String> cnslTp;
    Integer minPrice;
    Integer maxPrice;
    String[] hashTags;
}
