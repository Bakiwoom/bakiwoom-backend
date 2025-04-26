package com.javaex.idea.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javaex.idea.vo.CompanyVo;

@Repository
public class CompanyPageDao {

	@Autowired
	private SqlSession sqlSession;

	// 기업 정보 저장
	public void insertCompany(CompanyVo companyVo, int memberId) {
		Map<String, Object> map = new HashMap<>();
		map.put("companyVo", companyVo);
		map.put("memberId", memberId);
		sqlSession.insert("company.insertCompany", map);
	}

	// 기업 정보 수정
	public void updateCompany(CompanyVo companyVo) {
		sqlSession.update("company.updateCompany", companyVo);
	}

	// 회원 ID로 기업 정보 조회
	public CompanyVo getCompanyByMemberId(int memberId) {
		return sqlSession.selectOne("company.getCompanyByMemberId", memberId);
	}

}
