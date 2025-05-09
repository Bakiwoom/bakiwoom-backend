package com.javaex.idea.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.javaex.idea.dao.UserMyPageDao;
import com.javaex.idea.vo.JobPostingVo;
import com.javaex.idea.vo.UserVo;

@Service
public class UserMyPageService {
	
	@Autowired
	private UserMyPageDao userMypageDao;
	@Autowired
	private S3Service s3Service;

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
	
	//수정할 정보 보내기
	public int exeUpdateEdit(Map<String,Object> editDataMap) {
		
		UserVo userVo = (UserVo) editDataMap.get("editVo");
		
		// 1.s3 업로드 처리
		MultipartFile userProfile = (MultipartFile) editDataMap.get("userProfile");
		
		try {
			if(userProfile != null && !userProfile.isEmpty()) {
				String userProfileKey = "images/" + UUID.randomUUID() + "_" + userProfile.getOriginalFilename();
	            String userProfileImageUrl = s3Service.uploadFile(userProfileKey, userProfile.getInputStream(),
	            		userProfile.getSize(), userProfile.getContentType());
	            
	            userVo.setUserProfileImageUrl(userProfileImageUrl);
			}
		} catch (IOException e) {
			throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
		}
		
		// 2.회원정보 수정
		// 2-1.memberT update
		int result = userMypageDao.editMemberData(userVo);
		
		// 2-2.userT update
		if(result > 0) {
			int count = userMypageDao.editUserData(userVo);
			return count;
		}else {
			int count = 0;
			return count;
		}
		
	};
	
	
	
	
	
	
	
}; //
