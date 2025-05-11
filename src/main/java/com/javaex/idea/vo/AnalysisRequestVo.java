package com.javaex.idea.vo;

public class AnalysisRequestVo {
    private int userId;
    private int jobId;
    private Object jobInfo; // FastAPI에 넘길 용도, 사용 안 해도 있어야 JSON 구조 맞음

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public Object getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(Object jobInfo) {
        this.jobInfo = jobInfo;
    }
}
