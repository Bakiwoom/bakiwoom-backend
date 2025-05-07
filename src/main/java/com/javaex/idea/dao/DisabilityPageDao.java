package com.javaex.idea.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javaex.idea.vo.BookmarkVo;
import com.javaex.idea.vo.JobPostingVo;

@Repository
public class DisabilityPageDao {
	
	@Autowired SqlSession sqlSession;

	//전체 공고글 가져오기
	public List<JobPostingVo> getList() {
		List<JobPostingVo> allList = sqlSession.selectList("disability.getList");
		return allList;
	};
	
	//북마크리스트 가져오기
	public List<BookmarkVo> getBookmarkList(int userId) {
		List<BookmarkVo> bookmarkList = sqlSession.selectList("disability.bookmarkList", userId);
		return bookmarkList;
	}
	
	//북마크중복체크
	public boolean CheckBookmark(JobPostingVo idsVo) {
		boolean check = sqlSession.selectOne("disability.checkBookmark", idsVo);
		return check;
	};
	
	//북마크 등록
	public int insertBookmark(JobPostingVo idsVo) {
		int result = sqlSession.insert("disability.insertBookmark", idsVo);
		return result;
	};
	
	//북마크 삭제
	public int deleteBookmark(JobPostingVo idsVo) {
		int result = sqlSession.delete("disability.deleteBookmark", idsVo);
		return result;
	}
	
	
	
	
};
