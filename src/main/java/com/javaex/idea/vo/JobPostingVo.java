package com.javaex.idea.vo;

public class JobPostingVo {

	private int jobId;
	private int companyId;
	private int userId;
	private String title;
	private String department;
	private String status;
	private String location;
	private String jobType;
	private String employmentType;
	private String experienceLevel;
	private String experienceYears;
	private String deadline;
	private String description;
	private String requirements;
	private String preferred;
	private String disabilityType;
	private int viewCount;
	private int applyCount;
	private int bookmarkCount;
	private String createdAt;
	private String updatedAt;
	private String appliedAt;
	// 조인 결과용 추가 필드
	private String userName;
	private String companyName;
	private String companyLogo;
	private String disabilityTypeName;
	private double popularityScore;
	private String role;

	//사용자가 이미 지원했는지 여부
	private boolean hasApplied;

	// ✅ 추가할 항목
	private String myBenefits;
	private String companyBenefits;

	public JobPostingVo() {

	}

	public JobPostingVo(int jobId, int companyId, int userId, String title, String department, String status,
			String location, String jobType, String employmentType, String experienceLevel, String experienceYears,
			String deadline, String description, String requirements, String preferred, String disabilityType,
			int viewCount, int applyCount, int bookmarkCount, String createdAt, String updatedAt, String userName,
			String companyName, String companyLogo, String disabilityTypeName, double popularityScore, String role) {
		this.jobId = jobId;
		this.companyId = companyId;
		this.userId = userId;
		this.title = title;
		this.department = department;
		this.status = status;
		this.location = location;
		this.jobType = jobType;
		this.employmentType = employmentType;
		this.experienceLevel = experienceLevel;
		this.experienceYears = experienceYears;
		this.deadline = deadline;
		this.description = description;
		this.requirements = requirements;
		this.preferred = preferred;
		this.disabilityType = disabilityType;
		this.viewCount = viewCount;
		this.applyCount = applyCount;
		this.bookmarkCount = bookmarkCount;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.userName = userName;
		this.companyName = companyName;
		this.companyLogo = companyLogo;
		this.disabilityTypeName = disabilityTypeName;
		this.popularityScore = popularityScore;
		this.role = role;
	}

	public boolean isHasApplied() {
		return hasApplied;
	}
	public void setHasApplied(boolean hasApplied) {
		this.hasApplied = hasApplied;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(String employmentType) {
		this.employmentType = employmentType;
	}

	public String getExperienceLevel() {
		return experienceLevel;
	}

	public void setExperienceLevel(String experienceLevel) {
		this.experienceLevel = experienceLevel;
	}

	public String getExperienceYears() {
		return experienceYears;
	}

	public void setExperienceYears(String experienceYears) {
		this.experienceYears = experienceYears;
	}

	public String getDeadline() {
		return deadline;
	}

	public void setDeadline(String deadline) {
		this.deadline = deadline;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRequirements() {
		return requirements;
	}

	public void setRequirements(String requirements) {
		this.requirements = requirements;
	}

	public String getPreferred() {
		return preferred;
	}

	public void setPreferred(String preferred) {
		this.preferred = preferred;
	}

	public String getDisabilityType() {
		return disabilityType;
	}

	public void setDisabilityType(String disabilityType) {
		this.disabilityType = disabilityType;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getApplyCount() {
		return applyCount;
	}

	public void setApplyCount(int applyCount) {
		this.applyCount = applyCount;
	}

	public int getBookmarkCount() {
		return bookmarkCount;
	}

	public void setBookmarkCount(int bookmarkCount) {
		this.bookmarkCount = bookmarkCount;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyLogo() {
		return companyLogo;
	}

	public void setCompanyLogo(String companyLogo) {
		this.companyLogo = companyLogo;
	}

	public String getDisabilityTypeName() {
		return disabilityTypeName;
	}

	public void setDisabilityTypeName(String disabilityTypeName) {
		this.disabilityTypeName = disabilityTypeName;
	}

	public double getPopularityScore() {
		return popularityScore;
	}

	public void setPopularityScore(double popularityScore) {
		this.popularityScore = popularityScore;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "JobPostingVo [jobId=" + jobId + ", companyId=" + companyId + ", userId=" + userId + ", title=" + title
				+ ", department=" + department + ", status=" + status + ", location=" + location + ", jobType="
				+ jobType + ", employmentType=" + employmentType + ", experienceLevel=" + experienceLevel
				+ ", experienceYears=" + experienceYears + ", deadline=" + deadline + ", description=" + description
				+ ", requirements=" + requirements + ", preferred=" + preferred + ", disabilityType=" + disabilityType
				+ ", viewCount=" + viewCount + ", applyCount=" + applyCount + ", bookmarkCount=" + bookmarkCount
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", userName=" + userName + ", companyName="
				+ companyName + ", companyLogo=" + companyLogo + ", disabilityTypeName=" + disabilityTypeName
				+ ", popularityScore=" + popularityScore + ", role=" + role + "]";
	}

    public String getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(String appliedAt) {
        this.appliedAt = appliedAt;
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
