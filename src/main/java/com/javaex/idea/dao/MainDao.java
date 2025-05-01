package com.javaex.idea.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javaex.idea.vo.BookmarkVo;
import com.javaex.idea.vo.JobPostingVo;

@Repository
public class MainDao {

	@Autowired
	private SqlSession sqlSession;

	// 맞춤 공고 조회
	public List<JobPostingVo> getRecommendedJobs(int memberId) {
		return sqlSession.selectList("main.getRecommendedJobs", memberId);
	}

	// 인기 공고 조회
	public List<JobPostingVo> getPopularJobs() {
		return sqlSession.selectList("main.getPopularJobs");
	}

	// 주목받는 공고 조회
	public List<JobPostingVo> getTrendingJobs() {
		return sqlSession.selectList("main.getTrendingJobs");
	}

	// 북마크 체크
	public int checkBookmark(BookmarkVo bookmarkVo) {
		return sqlSession.selectOne("main.checkBookmark", bookmarkVo);
	}

	// 북마크 추가
	public void addBookmark(BookmarkVo bookmarkVo) {
		sqlSession.insert("main.addBookmark", bookmarkVo);
	}

	// 북마크 삭제
	public int removeBookmark(int userId, int jobId) {
		BookmarkVo bookmarkVo = new BookmarkVo();
		bookmarkVo.setUserId(userId);
		bookmarkVo.setJobId(jobId);
		return sqlSession.delete("main.removeBookmark", bookmarkVo);
	}

	// 사용자의 북마크 목록 조회
	public List<BookmarkVo> getUserBookmarks(int userId) {
		return sqlSession.selectList("main.getUserBookmarks", userId);
	}

	// 북마크 카운트 증가
	public void increaseBookmarkCount(int jobId) {
		sqlSession.update("main.increaseBookmarkCount", jobId);
	}

	// 북마크 카운트 감소
	public void decreaseBookmarkCount(int jobId) {
		sqlSession.update("main.decreaseBookmarkCount", jobId);
	}

}
