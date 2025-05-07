package com.javaex.idea.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.javaex.idea.dao.MainDao;
import com.javaex.idea.vo.BookmarkVo;
import com.javaex.idea.vo.JobPostingVo;

@Service
public class MainService {

	@Autowired
	private MainDao mainDao;

	// member_id로 user_id 조회 - 맨 위에 추가
	public Integer getUserIdByMemberId(int memberId) {
		return mainDao.getUserIdByMemberId(memberId);
	}

	// 맞춤 공고 조회 - 기존 메소드 수정
	public List<JobPostingVo> getRecommendedJobs(int memberId) {
		// member_id로 user_id 조회
		Integer userId = getUserIdByMemberId(memberId);

		if (userId == null) {
			return null; // 사용자 정보가 없는 경우
		}

		return mainDao.getRecommendedJobs(userId);
	}

	// 인기 공고 조회
	public List<JobPostingVo> getPopularJobs() {
		return mainDao.getPopularJobs();
	}

	// 주목받는 공고 조회
	public List<JobPostingVo> getTrendingJobs() {
		return mainDao.getTrendingJobs();
	}

	// 북마크 추가
	public boolean addBookmark(BookmarkVo bookmarkVo) {
		// 이미 북마크가 있는지 확인
		int count = mainDao.checkBookmark(bookmarkVo);
		if (count > 0) {
			return false;
		}

		// 북마크 추가
		mainDao.addBookmark(bookmarkVo);

		// job_posting의 bookmark_count 증가
		mainDao.increaseBookmarkCount(bookmarkVo.getJobId());

		return true;
	}

	// 북마크 삭제
	public boolean removeBookmark(int userId, int jobId) {
		// 북마크 삭제
		int result = mainDao.removeBookmark(userId, jobId);

		if (result > 0) {
			// job_posting의 bookmark_count 감소
			mainDao.decreaseBookmarkCount(jobId);
			return true;
		}

		return false;
	}

	// 사용자의 북마크 목록 조회
	public List<BookmarkVo> getUserBookmarks(int userId) {
		return mainDao.getUserBookmarks(userId);
	}

}
