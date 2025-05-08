package com.javaex.idea.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.javaex.idea.dao.UserMyPageDao;
import com.javaex.idea.vo.JobPostingVo;
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
	
	//user 지원현황 가져오기
	public Map<String, Object> getApplications(int userId) {
		
		Map<String, Object> userApplicationMap = new HashMap<>();
		
		//지원공고글 토탈 갯수
		int totalCount = userMypageDao.getApplicationCount(userId);
		userApplicationMap.put("totalCount", totalCount);
		
		//지원공고 list
		List<JobPostingVo> applicationList = userMypageDao.getApplications(userId);
		userApplicationMap.put("applicationList", applicationList);
			//list 비어있으면 imEmpty값 true
		userApplicationMap.put("isEmpty", applicationList == null || applicationList.isEmpty());
		
		
		return userApplicationMap;
	};
	
	//- edit page -
	
	//수정할 정보 가져오기
	public UserVo getEdit(int userId) {
		UserVo getEditVo = userMypageDao.getEdit(userId);
		return getEditVo;
	};
	
	
	
	
	
	
	
	
}; //
