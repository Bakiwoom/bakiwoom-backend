package com.javaex.idea.controller;

import com.javaex.idea.service.JobPostingService;
import com.javaex.idea.vo.JobPostingVo;
import com.javaex.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job")
public class JobPostingController {

    @Autowired
    private JobPostingService jobPostingService;

    // 공고 등록
    @PostMapping("/create")
    public ResponseEntity<?> createJob(@RequestBody JobPostingVo jobPostingVo, HttpServletRequest request) {
        try {
            // JWT에서 memberId 추출
            Integer memberId = JwtUtil.getNoFromHeader(request);
            if (memberId == null) {
                return ResponseEntity.status(401).body("인증 정보가 없습니다.");
            }

            // companyId만 조회
            Integer companyId = jobPostingService.getCompanyIdByMemberId(memberId);
            if (companyId == null) {
                return ResponseEntity.badRequest().body("회원에 연결된 기업 정보가 없습니다.");
            }

            // 공고 객체 세팅
            jobPostingVo.setCompanyId(companyId);
            jobPostingVo.setStatus("open");

            // 등록
            jobPostingService.insertJobPosting(jobPostingVo);

            // 결과 반환
            return ResponseEntity.ok().body(Map.of("result", "success", "jobId", jobPostingVo.getJobId()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("공고 등록 중 오류 발생: " + e.getMessage());
        }
    }


    // 공고 상세 조회 (선택)
    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJobDetail(@PathVariable int jobId) {
        JobPostingVo job = jobPostingService.getJobPostingById(jobId);
        return ResponseEntity.ok(job);
    }

    // 공고 수정 (선택)
    @PutMapping("/{jobId}")
    public ResponseEntity<?> updateJob(@PathVariable int jobId, @RequestBody JobPostingVo jobPostingVo) {
        jobPostingVo.setJobId(jobId);
        jobPostingService.updateJobPosting(jobPostingVo);
        return ResponseEntity.ok("updated");
    }

    // 공고 삭제
    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> deleteJob(@PathVariable int jobId, HttpServletRequest request) {
        // (선택) JWT로 인증·권한 체크
        Integer memberId = JwtUtil.getNoFromHeader(request);
        if (memberId == null) {
            return ResponseEntity.status(401).body(Map.of("result", "fail", "message", "인증 정보가 없습니다."));
        }

        jobPostingService.deleteJobPosting(jobId);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 공고 마감 처리
    @PatchMapping("/{jobId}/close")
    public ResponseEntity<?> closeJob(@PathVariable int jobId, HttpServletRequest request) {
        // (선택) JWT로 인증·권한 체크
        Integer memberId = JwtUtil.getNoFromHeader(request);
        if (memberId == null) {
            return ResponseEntity.status(401).body(Map.of("result", "fail", "message", "인증 정보가 없습니다."));
        }

        jobPostingService.closeJobPosting(jobId);
        return ResponseEntity.ok(Map.of("result", "success"));
    }
}
