package com.javaex.idea.vo;

public class BookmarkVo {

	private int bookmarkId;
	private int userId;
	private int jobId;
	private String createdAt;

	public BookmarkVo() {

	}

	public BookmarkVo(int bookmarkId, int userId, int jobId, String createdAt) {
		this.bookmarkId = bookmarkId;
		this.userId = userId;
		this.jobId = jobId;
		this.createdAt = createdAt;
	}

	public int getBookmarkId() {
		return bookmarkId;
	}

	public void setBookmarkId(int bookmarkId) {
		this.bookmarkId = bookmarkId;
	}

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

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "BookmarkVo [bookmarkId=" + bookmarkId + ", userId=" + userId + ", jobId=" + jobId + ", createdAt="
				+ createdAt + "]";
	}

}
