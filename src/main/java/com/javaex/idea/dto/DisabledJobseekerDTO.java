package com.javaex.idea.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "disabled_jobseekers")
public class DisabledJobseekerDTO {
    @Id
    private String id; // 예: 연번 등 유니크 값
    private String 구직등록일;
    private String 기관분류;
    private int 연령;
    private int 연번;
    private String 장애유형;
    private String 중증여부;
    private String 희망임금;
    private String 희망지역;
    private String 희망직종;
}
