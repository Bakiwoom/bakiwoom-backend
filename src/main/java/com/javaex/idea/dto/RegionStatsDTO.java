package com.javaex.idea.dto;

import lombok.Data;
import java.util.Map;

@Data
public class RegionStatsDTO {
    private String region;                    // 지역명
    private long totalCount;                  // 전체 구직자 수
    private Map<String, Long> disabilityTypeDistribution;  // 장애유형별 분포
    private Map<String, Long> ageDistribution;            // 연령대별 분포
    private Map<String, Long> jobTypeDistribution;        // 희망직종별 분포
    private Map<String, Long> salaryDistribution;         // 희망임금별 분포
    private Map<String, Long> severityDistribution;       // 중증/경증 분포
} 