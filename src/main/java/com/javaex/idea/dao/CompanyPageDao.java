package com.javaex.idea.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javaex.idea.vo.ApplicationVo;
import com.javaex.idea.vo.CompanyManagerVo;
import com.javaex.idea.vo.CompanyVo;
import com.javaex.idea.vo.MemberVo;

@Repository
public class CompanyPageDao {

	@Autowired
	private SqlSession sqlSession;

	// 회원 ID로 기업 정보 조회
	public CompanyVo getCompanyByMemberId(int memberId) {
		return sqlSession.selectOne("company.getCompanyByMemberId", memberId);
	}

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

	// 담당자 정보 등록
	public void insertManager(CompanyManagerVo managerVo) {
		sqlSession.insert("company.insertManager", managerVo);
	}

	// 기업 ID로 담당자 정보 조회
	public CompanyManagerVo getManagerByCompanyId(int companyId) {
		return sqlSession.selectOne("company.getManagerByCompanyId", companyId);
	}

	// 담당자 정보 수정
	public void updateManager(CompanyManagerVo managerVo) {
		sqlSession.update("company.updateManager", managerVo);
	}

	// 회원 ID로 계정 정보 조회
	public MemberVo getMemberById(int memberId) {
		return sqlSession.selectOne("company.getMemberById", memberId);
	}

	// 비밀번호 변경
	public void updateMemberPassword(MemberVo memberVo) {
		try {
			int rows = sqlSession.update("company.updateMemberPassword", memberVo);
			if (rows == 0) {
				throw new RuntimeException("비밀번호 업데이트가 실행되지 않았습니다. 해당 회원이 존재하지 않을 수 있습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// 회사 ID로 지원 내역 조회
	public List<ApplicationVo> getApplicationsByCompanyId(int companyId) {
		return sqlSession.selectList("company.getApplicationsByCompanyId", companyId);
	}
}
