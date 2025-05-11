package com.javaex.idea.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "welfare_service_list")
public class WelfareServiceListDTO {
    @Id
    private String servId; // 고유값, 중복 없이 덮어쓰기
    private String inqNum;
    private String intrsThemaArray;
    private String jurMnofNm;
    private String jurOrgNm;
    private String lifeArray;
    private String onapPsbltYn;
    private String rprsCtadr;
    private String servDgst;
    private String servDtlLink;
    private String servNm;
    private String sprtCycNm;
    private String srvPvsnNm;
    private String svcfrstRegTs;
    private String trgterIndvdlArray;
} 