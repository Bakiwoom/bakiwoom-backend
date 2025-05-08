package com.javaex.idea.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javaex.idea.vo.JobPostingVo;
import com.javaex.idea.vo.UserVo;

@Repository
public class UserMyPageDao {
	
	@Autowired SqlSession sqlSession;

	//북마크 갯수 가져오기
	public int getBookmarkCount(int userId) {
		int count = sqlSession.selectOne("userMypage.getBookmarkCount",userId);
		return count;
	};
	
	//프로필이미지, 장애인증여부
	public UserVo getUserData(int userId) {
		UserVo userData = sqlSession.selectOne("userMypage.getUserData",userId);
		return userData;
	};
	
	//지원공고 갯수 가져오기
	public int getApplicationCount(int userId) {
		int totalCount = sqlSession.selectOne("userMypage.getApplicationCount",userId);
		return totalCount;
	};
	
	//지원공고글list 가져오기
	public List<JobPostingVo> getApplications(int userId) {
		List<JobPostingVo> applicationList = sqlSession.selectList("userMypage.getApplications", userId);
		return applicationList;
	};
	
	
	
	
	
	
}; //
