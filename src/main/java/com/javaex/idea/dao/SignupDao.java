package com.javaex.idea.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.javaex.idea.vo.SignupVo;

@Repository
public class SignupDao {
	
	@Autowired
	private SqlSession sqlSession;

	//중복체크
	public int checkId(String id) {
		int idCheckResult = sqlSession.selectOne("signup.checkId", id);
		return idCheckResult;
	};
	
	//member테이블 등록
	public int memberSignup(SignupVo memberVo) {
		int result = sqlSession.insert("signup.insertMember", memberVo);
		return result;
	}
	
	//member > user
	public int userSignup(SignupVo userVo) {
		int userResult = sqlSession.insert("signup.insertUser", userVo);
		return userResult;
	}
	
	//user 장애번호 select
	public int selectDisabilityId(String disabilityType) {
		int disabilityId = sqlSession.selectOne("signup.selectDisabilityId", disabilityType);
		return disabilityId;
	};
	
	//member > company
	public int companySignup(SignupVo companyVo) {
		int companyResult = sqlSession.insert("signup.insertCompany", companyVo);
		return companyResult;
	}
	
	//로그인
	public SignupVo login(SignupVo loginVo) {
		SignupVo authUser = sqlSession.selectOne("signup.login", loginVo);
		return authUser;
	};
	
	//로그인맴버 정보 가져오기 (role)
	public String getRole(int memberId) {
		String role = sqlSession.selectOne("signup.getRole",memberId);
		return role;
	};
	
	//로그인 > user (name,id)
	public SignupVo getUser(int memberId) {
		SignupVo userVo = sqlSession.selectOne("signup.loginUser",memberId);
		return userVo;
	};
	
	//로그인 > company (name,id)
	public SignupVo getCompany(int memberId) {
		SignupVo companyVo = sqlSession.selectOne("signup.loginCompany",memberId);
		return companyVo;
	};
	
	
	
	
	
	
	
}
