package com.javaex.idea.dao;

import com.javaex.idea.vo.ApplicationVo;
import com.javaex.idea.vo.JobPostingVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JobPostingDao {

    @Autowired
    private SqlSession sqlSession;

    public void insertJobPosting(JobPostingVo jobPostingVo) {
        sqlSession.insert("job.insertJobPosting", jobPostingVo);
    }

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

    /** 0) memberId → userId 조회 메서드 추가 **/
    public Integer getUserIdByMemberId(int memberId) {
        return sqlSession.selectOne("job.getUserIdByMemberId", memberId);
    }

    /** 1) 중복 지원 체크 **/
    public int countByUserIdAndJobId(Map<String, Object> params) {
        return sqlSession.selectOne("job.countByUserIdAndJobId", params);
    }

    /** 2) 지원 정보 저장 **/
    public void insertApplication(ApplicationVo vo) {
        sqlSession.insert("job.insertApplication", vo);
    }

    public List<ApplicationVo> getApplicationsByUserId(int userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return sqlSession.selectList("job.getApplicationsByUserId", params);
    }
    public void cancelApplication(Map<String, Object> params) {
        sqlSession.delete("job.cancelApplication", params);
    }
}
