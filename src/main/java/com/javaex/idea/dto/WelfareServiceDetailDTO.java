package com.javaex.idea.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
// 하위 DTO import
import com.javaex.idea.dto.ApplmetItemDTO;
import com.javaex.idea.dto.InqplCtadrItemDTO;
import com.javaex.idea.dto.InqplHmpgReldItemDTO;
import com.javaex.idea.dto.BasfrmItemDTO;
import com.javaex.idea.dto.BaslawItemDTO;

@Data
@Document(collection = "welfare_service_detail")
public class WelfareServiceDetailDTO {
    @Id
    private String servId; // 고유값, 중복 없이 덮어쓰기
    private String servNm;
    private String jurMnofNm;
    private String tgtrDtlCn;
    private String slctCritCn;
    private String alwServCn;
    private String crtrYr;
    private String rprsCtadr;
    private String wlfareInfoOutlCn;
    private String sprtCycNm;
    private String srvPvsnNm;
    private String lifeArray;
    private String trgterIndvdlArray;
    private String intrsThemaArray;
    private List<ApplmetItemDTO> applmetList;
    private List<InqplCtadrItemDTO> inqplCtadrList;
    private List<InqplHmpgReldItemDTO> inqplHmpgReldList;
    private List<BasfrmItemDTO> basfrmList;
    private List<BaslawItemDTO> baslawList;
    private String resultCode;
    private String resultMessage;
} 