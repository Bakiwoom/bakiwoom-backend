package com.javaex.idea.dao;

import com.javaex.idea.vo.AnalysisResultVo;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PolicyAnalysisDao {

    @Autowired
    private SqlSession sqlSession;

    public AnalysisResultVo getAnalysisResult(int userId, int jobId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("jobId", jobId);
        return sqlSession.selectOne("ai.getAnalysisResult", params);
    }
}