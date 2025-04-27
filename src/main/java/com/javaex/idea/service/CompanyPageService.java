package com.javaex.idea.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.javaex.idea.dao.CompanyPageDao;
import com.javaex.idea.vo.CompanyVo;

@Service
public class CompanyPageService {
	
	@Autowired
	private CompanyPageDao companyPageDao;
	
	// 기업 정보 저장
	public void saveCompanyInfo(CompanyVo companyVo, int memberId) {
        // 기존 기업 정보가 있는지 확인
        CompanyVo existingCompany = companyPageDao.getCompanyByMemberId(memberId);
        if (existingCompany != null) {
            // 업데이트
            companyVo.setCompanyId(existingCompany.getCompanyId());
            companyPageDao.updateCompany(companyVo);
        } else {
            // 새로 등록
            companyPageDao.insertCompany(companyVo, memberId);
        }
    }
	
	// 기업 정보 가져오기
	public CompanyVo getCompanyDetail(int memberId) {
        // member_id로 회사 정보 조회
        return companyPageDao.getCompanyByMemberId(memberId);
    }

}
