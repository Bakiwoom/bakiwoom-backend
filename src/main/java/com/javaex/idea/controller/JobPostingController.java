package com.javaex.idea.controller;

import com.javaex.idea.service.JobPostingService;
import com.javaex.idea.vo.JobPostingVo;
import com.javaex.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public ResponseEntity<?> getJobDetail(
            @PathVariable int jobId,
            HttpServletRequest request
    ) {
        try {
            JobPostingVo job = jobPostingService.getJobPostingById(jobId);

            Integer memberId = JwtUtil.getNoFromHeader(request);
            if (memberId == null) {
                // 비회원: 기본값
                job.setHasApplied(false);
            } else {
                // 회원: company 계정인지 확인
                Integer companyId = jobPostingService.getCompanyIdByMemberId(memberId);
                if (companyId == null) {
                    // 일반 유저 → 지원 여부 확인
                    int userId = jobPostingService.resolveUserId(memberId);
                    job.setHasApplied(jobPostingService.hasApplied(userId, jobId));
                } else {
                    // 회사 계정 → hasApplied 무의미하므로 false로 처리
                    job.setHasApplied(false);
                }
            }

            return ResponseEntity.ok(job);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("result", "fail", "message", "공고 상세 조회 중 오류가 발생했습니다."));
        }
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

    /** 사용자 지원 **/
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<?> applyJob(
            @PathVariable int jobId,
            HttpServletRequest request
    ) {
        // 1) JWT 에서 뽑아낸 건 memberId
        Integer memberId = JwtUtil.getNoFromHeader(request);
        if (memberId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("result","fail","message","로그인이 필요합니다."));
        }

        try {
            // 2) memberId → 실제 user.user_id 로 변환
            int userId = jobPostingService.resolveUserId(memberId);

            // 3) 이 userId 로 insert
            jobPostingService.applyToJob(userId, jobId);

            return ResponseEntity.ok(Map.of("result","success"));
        } catch (IllegalStateException ise) {
            return ResponseEntity.badRequest()
                    .body(Map.of("result","fail","message",ise.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("result","fail","message","지원 처리 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{jobId}/cancel")
    public ResponseEntity<?> cancelApplication(@PathVariable int jobId, HttpServletRequest request) {
        Integer memberId = JwtUtil.getNoFromHeader(request);
        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        try {
            int userId = jobPostingService.resolveUserId(memberId);
            jobPostingService.cancelApplication(userId, jobId);
            return ResponseEntity.ok(Map.of("result", "success"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("result", "fail", "message", e.getMessage()));
        }
    }
}
