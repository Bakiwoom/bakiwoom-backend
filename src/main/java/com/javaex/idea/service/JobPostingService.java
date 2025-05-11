// src/main/java/com/javaex/idea/service/JobPostingService.java
package com.javaex.idea.service;

import com.javaex.idea.dao.JobPostingDao;
import com.javaex.idea.vo.ApplicationVo;
import com.javaex.idea.vo.JobPostingVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class JobPostingService {

    @Autowired
    private JobPostingDao jobPostingDao;

    public void insertJobPosting(JobPostingVo jobPostingVo) {
        jobPostingDao.insertJobPosting(jobPostingVo);
    }

    public JobPostingVo getJobPostingById(int jobId) {
        return jobPostingDao.getJobPostingById(jobId);
    }

    public void updateJobPosting(JobPostingVo jobPostingVo) {
        jobPostingDao.updateJobPosting(jobPostingVo);
    }

    public void deleteJobPosting(int jobId) {
        jobPostingDao.deleteJobPosting(jobId);
    }

    // ↓ 새로 추가: 공고 마감 로직 위임
    public void closeJobPosting(int jobId) {
        jobPostingDao.closeJobPosting(jobId);
    }

    public Integer getCompanyIdByMemberId(int memberId) {
        return jobPostingDao.getCompanyIdByMemberId(memberId);
    }

    /** 1) 지원 여부 조회 **/
    public boolean hasApplied(int userId, int jobId) {
        return jobPostingDao.countByUserIdAndJobId(
                Map.of("userId", userId, "jobId", jobId)
        ) > 0;
    }

    /** memberId → userId 조회 **/
    public int resolveUserId(int memberId) {
        Integer userId = jobPostingDao.getUserIdByMemberId(memberId);
        if (userId == null) {
            throw new IllegalStateException("해당 멤버에 연결된 사용자 정보가 없습니다.");
        }
        return userId;
    }
    /** 2) 지원 처리 **/
    public void applyToJob(int userId, int jobId) {
        if (hasApplied(userId, jobId)) {
            throw new IllegalStateException("이미 지원하신 공고입니다.");
        }
        ApplicationVo vo = new ApplicationVo();
        vo.setUserId(userId);
        vo.setJobId(jobId);
        jobPostingDao.insertApplication(vo);
    }

    public void cancelApplication(int userId, int jobId) {
        Map<String, Object> params = Map.of(
                "userId", userId,
                "jobId", jobId
        );
        jobPostingDao.cancelApplication(params);
    }

}
