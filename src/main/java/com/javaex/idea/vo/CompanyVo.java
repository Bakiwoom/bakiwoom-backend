package com.javaex.idea.vo;

public class CompanyVo {
	private int companyId;
	private int memberId;
	private String name;
	private String businessNumber;
	private String businessType;
	private String size;
	private String foundingYear;
	private int employeeCount;
	private String address;
	private String phone;
	private String email;
	private String website;
	private String logo;
	private String intro;
	private String createdAt;
	private String updatedAt;

	public CompanyVo() {

	}

	public CompanyVo(int companyId, int memberId, String name, String businessNumber, String businessType, String size,
			String foundingYear, int employeeCount, String address, String phone, String email, String website,
			String logo, String intro, String createdAt, String updatedAt) {
		this.companyId = companyId;
		this.memberId = memberId;
		this.name = name;
		this.businessNumber = businessNumber;
		this.businessType = businessType;
		this.size = size;
		this.foundingYear = foundingYear;
		this.employeeCount = employeeCount;
		this.address = address;
		this.phone = phone;
		this.email = email;
		this.website = website;
		this.logo = logo;
		this.intro = intro;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBusinessNumber() {
		return businessNumber;
	}

	public void setBusinessNumber(String businessNumber) {
		this.businessNumber = businessNumber;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getFoundingYear() {
		return foundingYear;
	}

	public void setFoundingYear(String foundingYear) {
		this.foundingYear = foundingYear;
	}

	public int getEmployeeCount() {
		return employeeCount;
	}

	public void setEmployeeCount(int employeeCount) {
		this.employeeCount = employeeCount;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
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

	@Override
	public String toString() {
		return "CompanyVo [companyId=" + companyId + ", memberId=" + memberId + ", name=" + name + ", businessNumber="
				+ businessNumber + ", businessType=" + businessType + ", size=" + size + ", foundingYear="
				+ foundingYear + ", employeeCount=" + employeeCount + ", address=" + address + ", phone=" + phone
				+ ", email=" + email + ", website=" + website + ", logo=" + logo + ", intro=" + intro + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + "]";
	}

}
