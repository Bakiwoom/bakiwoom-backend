package com.javaex.idea.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.javaex.idea.service.UserMyPageService;
import com.javaex.idea.vo.UserVo;
import com.javaex.util.JsonResult;

@RestController
public class UserMyPageController {
	
	@Autowired UserMyPageService userMypageService;
	
	// -mypage main-
	//기본정보 가져오기(북마크갯수, 프로필이미지, 장애인증)
	@GetMapping(value="/api/mypage/bookmarkcount/{userId}")
	public JsonResult getUser(@PathVariable ("userId") int userId) {
		UserVo userVo = userMypageService.exeGetUser(userId);
		
		if(userVo != null) {
			return JsonResult.success(userVo);
		}else {
			return JsonResult.fail("실패");
		}
	};
	
	//user 지원현황 가져오기
	@GetMapping(value="/api/mypage/getApplications/{userId}")
	public JsonResult getApplications(@PathVariable ("userId") int userId) {
		Map<String, Object> userApplicationMap = userMypageService.getApplications(userId);
		
		return JsonResult.success(userApplicationMap);
	};
	
	
	// -mypage edit-
	//기존 회원정보 가져오기
	@GetMapping(value="/api/mypage/getEditUser/{userId}")
	public JsonResult getEditUser(@PathVariable("userId") int userId) {
		UserVo getEditVo = userMypageService.getEdit(userId);
		if(getEditVo != null) {
			return JsonResult.success(getEditVo);
		}else {
			return JsonResult.fail("실패");
		}
	};
	
	//수정할 회원정보 보내기
	@PostMapping(value="/api/mypage/postEditUser")
	public JsonResult postEditUser(@ModelAttribute UserVo editVo
							,@RequestParam(value="userProfile", required=false) MultipartFile userProfile) {
		
		Map<String,Object> editDataMap = new HashMap<>();
		editDataMap.put("editVo", editVo);
		editDataMap.put("userProfile", userProfile);
		
		// 프로필이미지 파일 확인
        if (userProfile != null && !userProfile.isEmpty()) {
            System.out.println("프로필이미지 파일 이름: " + userProfile.getOriginalFilename());
        } else {
            System.out.println("프로필이미지 파일이 업로드되지 않았습니다.");
        }
        
        int count = userMypageService.exeUpdateEdit(editDataMap);
        
        if(count > 0) {
        	return JsonResult.success(count);
        }else {
        	return JsonResult.fail("회원정보수정 실패");
        }
	};
	
	
}; //
