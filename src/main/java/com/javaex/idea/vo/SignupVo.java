package com.javaex.idea.vo;

public class SignupVo {
	
	private int memberId;
	private int userId;
	private int companyId;
	private int disabilityId;
	
	private String id;
	private String pw;
	private String role;
	private int state;
	
	private String phoneNumber;
	private String email;
	private String name;
	private String gender;
	private String disabilityType;	//장애유형
	private String disabilityURL;	//장애인등록증 url
	private int isVerified;			//장애인 검증여부

	private String businessNumber;
	private String businessLicense;
	private String companyName;
	
	public SignupVo() {
		
	}
	

	public SignupVo(int memberId, int userId, int companyId, int disabilityId, String id, String pw, String role,
			int state, String phoneNumber, String email, String name, String gender, String disabilityType,
			String disabilityURL, int isVerified, String businessNumber, String businessLicense, String companyName) {
		super();
		this.memberId = memberId;
		this.userId = userId;
		this.companyId = companyId;
		this.disabilityId = disabilityId;
		this.id = id;
		this.pw = pw;
		this.role = role;
		this.state = state;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.name = name;
		this.gender = gender;
		this.disabilityType = disabilityType;
		this.disabilityURL = disabilityURL;
		this.isVerified = isVerified;
		this.businessNumber = businessNumber;
		this.businessLicense = businessLicense;
		this.companyName = companyName;
	}




	public int getMemberId() {
		return memberId;
	}


	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}


	public int getUserId() {
		return userId;
	}


	public void setUserId(int userId) {
		this.userId = userId;
	}


	public int getCompanyId() {
		return companyId;
	}


	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getPw() {
		return pw;
	}


	public void setPw(String pw) {
		this.pw = pw;
	}


	public String getRole() {
		return role;
	}


	public void setRole(String role) {
		this.role = role;
	}


	public int getState() {
		return state;
	}


	public void setState(int state) {
		this.state = state;
	}


	public String getPhoneNumber() {
		return phoneNumber;
	}


	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDisabilityType() {
		return disabilityType;
	}


	public void setDisabilityType(String disabilityType) {
		this.disabilityType = disabilityType;
	}


	public String getDisabilityURL() {
		return disabilityURL;
	}


	public void setDisabilityURL(String disabilityURL) {
		this.disabilityURL = disabilityURL;
	}


	public int getIsVerified() {
		return isVerified;
	}


	public void setIsVerified(int isVerified) {
		this.isVerified = isVerified;
	}


	public String getBusinessNumber() {
		return businessNumber;
	}


	public void setBusinessNumber(String businessNumber) {
		this.businessNumber = businessNumber;
	}


	public String getBusinessLicense() {
		return businessLicense;
	}


	public void setBusinessLicense(String businessLicense) {
		this.businessLicense = businessLicense;
	}

	public int getDisabilityId() {
		return disabilityId;
	}

	public void setDisabilityId(int disabilityId) {
		this.disabilityId = disabilityId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	
	public String getGender() {
		return gender;
	}


	public void setGender(String gender) {
		this.gender = gender;
	}


	@Override
	public String toString() {
		return "SignupVo [memberId=" + memberId + ", userId=" + userId + ", companyId=" + companyId + ", disabilityId="
				+ disabilityId + ", id=" + id + ", pw=" + pw + ", role=" + role + ", state=" + state + ", phoneNumber="
				+ phoneNumber + ", email=" + email + ", name=" + name + ", gender=" + gender + ", disabilityType="
				+ disabilityType + ", disabilityURL=" + disabilityURL + ", isVerified=" + isVerified
				+ ", businessNumber=" + businessNumber + ", businessLicense=" + businessLicense + ", companyName="
				+ companyName + "]";
	}


	
}
