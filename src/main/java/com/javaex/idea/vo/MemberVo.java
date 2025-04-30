package com.javaex.idea.vo;

public class MemberVo {
	private int memberId;
	private String id;
	private String password;
	private String role;
	private int state;

	public MemberVo() {

	}

	public MemberVo(int memberId, String id, String password, String role, int state) {
		this.memberId = memberId;
		this.id = id;
		this.password = password;
		this.role = role;
		this.state = state;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	@Override
	public String toString() {
		return "MemberVo [memberId=" + memberId + ", id=" + id + ", password=" + password + ", role=" + role
				+ ", state=" + state + "]";
	}

}
