package com.javaex.idea.controller;

import com.javaex.idea.service.PolicyAnalysisService;
import com.javaex.idea.vo.AnalysisResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class PolicyAnalysisController {

    @Autowired
    private PolicyAnalysisService policyAnalysisService;

    @PostMapping("/benefits")
    public ResponseEntity<?> forwardAnalysis(@RequestBody Map<String, Object> body) {
        RestTemplate restTemplate = new RestTemplate();

        // ✅ FastAPI가 요구하는 구조로 body 구성
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_info", body.get("user_info"));
        payload.put("job_info", body.get("job_info"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8000/analyze/benefits",
                request,
                String.class
        );

        return ResponseEntity.ok("분석 요청 전송 완료");
    }

}