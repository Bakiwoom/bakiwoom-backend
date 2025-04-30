package com.javaex.idea.vo;

public class ApplicationVo {

	private int applicationId; // 지원 ID
	private int userId; // 사용자 ID
	private String userName; // 사용자 이름
	private String userPhone; // 사용자 연락처
	private int jobId; // 채용공고 ID
	private String jobTitle; // 채용공고 제목
	private String disabilityType; // 장애 유형/등급
	private String appliedDate; // 지원 일자
	private String createdAt;

	public ApplicationVo() {

	}

	public ApplicationVo(int applicationId, int userId, String userName, String userPhone, int jobId, String jobTitle,
			String disabilityType, String appliedDate, String createdAt) {
		this.applicationId = applicationId;
		this.userId = userId;
		this.userName = userName;
		this.userPhone = userPhone;
		this.jobId = jobId;
		this.jobTitle = jobTitle;
		this.disabilityType = disabilityType;
		this.appliedDate = appliedDate;
		this.createdAt = createdAt;
	}

	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getDisabilityType() {
		return disabilityType;
	}

	public void setDisabilityType(String disabilityType) {
		this.disabilityType = disabilityType;
	}

	public String getAppliedDate() {
		return appliedDate;
	}

	public void setAppliedDate(String appliedDate) {
		this.appliedDate = appliedDate;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "ApplicationVo [applicationId=" + applicationId + ", userId=" + userId + ", userName=" + userName
				+ ", userPhone=" + userPhone + ", jobId=" + jobId + ", jobTitle=" + jobTitle + ", disabilityType="
				+ disabilityType + ", appliedDate=" + appliedDate + ", createdAt=" + createdAt + "]";
	}

}
