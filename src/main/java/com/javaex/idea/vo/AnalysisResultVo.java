package com.javaex.idea.vo;

public class AnalysisResultVo {
    private int userId;
    private int jobId;
    private String myBenefits;
    private String companyBenefits;

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

    public String getMyBenefits() {
        return myBenefits;
    }

    public void setMyBenefits(String myBenefits) {
        this.myBenefits = myBenefits;
    }

    public String getCompanyBenefits() {
        return companyBenefits;
    }

    public void setCompanyBenefits(String companyBenefits) {
        this.companyBenefits = companyBenefits;
    }
}