package com.javaex.idea.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.javaex.idea.dao.UserMyPageDao;
import com.javaex.idea.vo.UserVo;

@Service
public class UserMyPageService {
	
	@Autowired UserMyPageDao userMypageDao;

	//기본정보 가져오기(북마크갯수, 프로필이미지, 장애인증)
	public UserVo exeGetUser(int userId) {
		UserVo userVo = new UserVo();
		
		//1.북마크갯수
		int count = userMypageDao.getBookmarkCount(userId);
		
		//2.프로필이미지, 장애인증여부
		UserVo userData = userMypageDao.getUserData(userId);
		
		// + 
		userVo.setBookmarkcount(count);
		userVo.setUserProfileImageUrl(userData.getUserProfileImageUrl());
		userVo.setIsVerified(userData.getIsVerified());
		
		return userVo;
	};
	
	
	
	
	
	
	
	
}; //
