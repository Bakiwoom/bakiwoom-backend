package com.javaex.idea.service;

import com.javaex.idea.dao.PolicyAnalysisDao;
import com.javaex.idea.vo.AnalysisResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PolicyAnalysisService {

    @Autowired
    private PolicyAnalysisDao policyAnalysisDao;

    public AnalysisResultVo getAnalysis(int userId, int jobId) {
        return policyAnalysisDao.getAnalysisResult(userId, jobId);
    }
}