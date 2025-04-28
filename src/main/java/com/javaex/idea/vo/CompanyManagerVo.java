package com.javaex.idea.vo;

public class CompanyManagerVo {

	private int managerId;
	private int companyId;
	private String name;
	private String position;
	private String phone;
	private String email;
	private String department;
	private String createdAt;
	private String updatedAt;

	public CompanyManagerVo() {

	}

	public CompanyManagerVo(int managerId, int companyId, String name, String position, String phone, String email,
			String department, String createdAt, String updatedAt) {
		this.managerId = managerId;
		this.companyId = companyId;
		this.name = name;
		this.position = position;
		this.phone = phone;
		this.email = email;
		this.department = department;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public int getManagerId() {
		return managerId;
	}

	public void setManagerId(int managerId) {
		this.managerId = managerId;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
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

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
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
		return "CompanyManagerVo [managerId=" + managerId + ", companyId=" + companyId + ", name=" + name
				+ ", position=" + position + ", phone=" + phone + ", email=" + email + ", department=" + department
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}

}
