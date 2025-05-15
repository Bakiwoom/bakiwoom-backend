package com.javaex.idea.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DisabilityTypeStatsDTO {
    // 기본 통계 정보
    private String disabilityType;        // 장애유형
    private long totalCount;              // 전체 장애인 수
    private long disabilityTypeCount;     // 해당 장애유형 인원수
    private double percentage;            // 비율(%)
    
    // 중증/경증 분류
    private Map<String, Long> severityDistribution;  // 중증여부별 분포
    
    // 지역별 분포 (시/도 기준)
    private Map<String, Long> regionDistribution;    // 지역별 분포
    
    // 연령대별 분포
    private Map<String, Long> ageDistribution;       // 연령대별 분포
    
    // 희망직종별 분포 
    private Map<String, Long> jobTypeDistribution;   // 직종별 분포
    
    // 희망임금별 분포
    private Map<String, Long> salaryDistribution;    // 희망임금별 분포
    
    // 다차원 분석 결과 - 복합 필터링 (중증도+지역+연령+직종+임금)
    private List<CombinedStatItem> combinedStats;

    @Data
    public static class CombinedStatItem {
        private String severity;      // 중증여부
        private String region;        // 지역
        private String ageGroup;      // 연령대
        private String jobType;       // 직종
        private String salary;        // 임금
        private long count;           // 해당 조합 인원수
        private double percentOfTotal; // 전체 대비 비율(%)
    }
} 