package com.javaex.idea.dao;

import com.javaex.idea.vo.JobPostingVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class JobPostingDao {

    @Autowired
    private SqlSession sqlSession;

    public void insertJobPosting(JobPostingVo jobPostingVo) {
        sqlSession.insert("job.insertJobPosting", jobPostingVo);
    }

    // ← 여기 매퍼 아이디를 selectJobById 로 맞춤
    public JobPostingVo getJobPostingById(int jobId) {
       // 매퍼의 <select id="selectJobById"> 에 맞춰 호출 아이디를 수정합니다.
          return sqlSession.selectOne("job.getJobPostingById", jobId);
    }
    public void updateJobPosting(JobPostingVo jobPostingVo) {
        sqlSession.update("job.updateJobPosting", jobPostingVo);
    }

    // 삭제도 @Param으로 이름 지정
    public void deleteJobPosting(@Param("jobId") int jobId) {
        sqlSession.delete("job.deleteJobPosting", jobId);
    }

    // 마감 처리 메서드
    public void closeJobPosting(@Param("jobId") int jobId) {
        sqlSession.update("job.closeJobPosting", jobId);
    }

    public Integer getCompanyIdByMemberId(int memberId) {
        return sqlSession.selectOne("job.getCompanyIdByMemberId", memberId);
    }
}
