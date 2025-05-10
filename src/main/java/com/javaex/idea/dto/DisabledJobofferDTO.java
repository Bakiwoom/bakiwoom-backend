package com.javaex.idea.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "disabled_job_offers")
public class DisabledJobofferDTO {
    
    @Id
    private String id; // rno + "_" + rnum 조합으로 파싱 시 할당
    
    private String busplaName;    // 사업장명
    private String cntctNo;       // 연락처
    private String compAddr;      // 사업장 주소
    private String empType;       // 고용형태
    private String enterType;     // 입사형태
    private String jobNm;         // 모집직종
    private String offerregDt;    // 구인신청일
    private String regDt;         // 등록일
    private String regagnName;    // 담당기관
    private String reqCareer;     // 요구경력
    private String reqEduc;       // 요구학력
    private String rno;           // 연번
    private String rnum;          // 순번
    private String salary;        // 임금
    private String salaryType;    // 임금형태
    private String termDate;      // 모집기간
    // 환경 관련 필드
    private String envBothHands;  // 양손 사용
    private String envEyesight;   // 시력
    private String envHandwork;   // 손작업
    private String envLiftPower;  // 들어올림
    private String envLstnTalk;   // 청취/대화
    private String envStndWalk;   // 서기/걷기
} 