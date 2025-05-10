// src/main/java/com/javaex/idea/service/JobPostingService.java
package com.javaex.idea.service;

import com.javaex.idea.dao.JobPostingDao;
import com.javaex.idea.vo.JobPostingVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
