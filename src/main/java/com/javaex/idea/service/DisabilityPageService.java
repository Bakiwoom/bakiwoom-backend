package com.javaex.idea.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.javaex.idea.dao.DisabilityPageDao;
import com.javaex.idea.dao.SignupDao;
import com.javaex.idea.vo.BookmarkVo;
import com.javaex.idea.vo.JobPostingVo;
import com.javaex.util.JsonResult;

@Service
public class DisabilityPageService {
	
	@Autowired DisabilityPageDao DPDao;
	@Autowired SignupDao signupDao;
	
	//전체 공고글 가져오기
	public List<JobPostingVo> exeGetList() {
		List<JobPostingVo> allList = DPDao.getList();
		return allList;
	};
	
	//북마크 가져오기
	public List<BookmarkVo> exeGetBookmarkList(int userId) {
		List<BookmarkVo> bookmarkList = DPDao.getBookmarkList(userId);
		return bookmarkList;
	};
	
	//북마크 등록
	public int exeInsertBookmark(int jobId, int userId) {
		
		JobPostingVo idsVo = new JobPostingVo();
		idsVo.setJobId(jobId);
		idsVo.setUserId(userId);
		
		//true:중복
		boolean check = DPDao.CheckBookmark(idsVo);
		
		if(!check) {
			int result = DPDao.insertBookmark(idsVo);
			return result;
		}else {
			return 0;
		}
	};
	
	//북마크 삭제
	public int exeDeleteBookmark(int jobId, int userId) {
		JobPostingVo idsVo = new JobPostingVo();
		idsVo.setJobId(jobId);
		idsVo.setUserId(userId);
		
		int result = DPDao.deleteBookmark(idsVo);
		
		return result;
	};

	
	
	
	
	
	
	
	
}
