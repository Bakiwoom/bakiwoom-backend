package com.javaex.idea.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javaex.idea.service.DisabilityPageService;
import com.javaex.idea.vo.BookmarkVo;
import com.javaex.idea.vo.JobPostingVo;
import com.javaex.util.JsonResult;

@RestController
public class DisabilityPageController {
	
	@Autowired DisabilityPageService DPService;
	
	//전체 공고글 가져오기
	@GetMapping(value="/api/disability")
	public JsonResult getList() {
		List<JobPostingVo> allList = DPService.exeGetList();
		return JsonResult.success(allList);
	};
	
	//유저별 북마크 리스트 가져오기
	@GetMapping(value="/api/disability/bookmarkList/{userId}")
	public JsonResult getBookmarkList(@PathVariable ("userId") int userId) {
		List<BookmarkVo> bookmarkList = DPService.exeGetBookmarkList(userId);
		return JsonResult.success(bookmarkList);
	};
	
	//북마크 등록
	@PostMapping(value="/api/disability/bookmark/{jobId}/{userId}")
	public JsonResult insertBookmark(@PathVariable ("jobId") int jobId
							  ,@PathVariable ("userId") int userId) {
		int result = DPService.exeInsertBookmark(jobId, userId);
		
		if(result > 0) {
			return JsonResult.success("북마크 등록 성공");
		}else {
			return JsonResult.fail("북마크 등록 실패");
		}
		
	};
	
	//북마크 삭제
	@DeleteMapping(value="/api/disability/bookmark/{jobId}/{userId}")
	public JsonResult deleteBookmark(@PathVariable ("jobId") int jobId
							  ,@PathVariable ("userId") int userId) {
		
		int result = DPService.exeDeleteBookmark(jobId, userId);
		
		if(result > 0) {
			return JsonResult.success("북마크 삭제 성공");
		}else {
			return JsonResult.fail("삭제할 북마크 없음");
		}
	};
	
	
	

} //
