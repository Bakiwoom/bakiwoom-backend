package com.javaex.idea.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javaex.idea.service.UserMyPageService;
import com.javaex.idea.vo.UserVo;
import com.javaex.util.JsonResult;

@RestController
public class UserMyPageController {
	
	@Autowired UserMyPageService userMypageService;
	
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
	
	
	
	
}; //
