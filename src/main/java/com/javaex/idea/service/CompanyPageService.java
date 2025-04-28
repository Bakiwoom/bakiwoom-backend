package com.javaex.idea.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.javaex.idea.dao.CompanyPageDao;
import com.javaex.idea.vo.CompanyManagerVo;
import com.javaex.idea.vo.CompanyVo;

@Service
public class CompanyPageService {

	@Autowired
	private CompanyPageDao companyPageDao;

	@Autowired
	private S3Service s3Service;

	// 기업 정보 조회
	public CompanyVo getCompanyDetail(int memberId) {
		// member_id로 회사 정보 조회
		return companyPageDao.getCompanyByMemberId(memberId);
	}

	// 기업 정보 등록
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

	// 기업 정보 수정
	public void updateCompany(CompanyVo companyVo, int memberId, MultipartFile logoFile) {
		try {
			// 기존 회사 정보 조회
			CompanyVo existingCompany = companyPageDao.getCompanyByMemberId(memberId);

			if (existingCompany == null) {
				throw new RuntimeException("회사 정보가 존재하지 않습니다.");
			}

			// companyId 설정
			companyVo.setCompanyId(existingCompany.getCompanyId());

			// 로고 파일 처리
			if (logoFile != null && !logoFile.isEmpty()) {
				// 새 로고 업로드
				String logoUrl = s3Service.uploadFile(logoFile);
				companyVo.setLogo(logoUrl);
			} else {
				// 로고 파일이 없으면 기존 로고 유지
				companyVo.setLogo(existingCompany.getLogo());
			}

			// 회사 정보 업데이트
			companyPageDao.updateCompany(companyVo);

		} catch (Exception e) {
			throw new RuntimeException("회사 정보 수정 중 오류가 발생했습니다.");
		}
	}

	// 담당자 정보 조회
	public CompanyManagerVo getManagerDetail(int memberId) {
		try {
			// 기업 정보 조회
			CompanyVo companyVo = companyPageDao.getCompanyByMemberId(memberId);
			if (companyVo == null) {
				return null; // 기업 정보가 없으면 담당자 정보도 없음
			}

			// 기업 ID로 담당자 정보 조회
			return companyPageDao.getManagerByCompanyId(companyVo.getCompanyId());
		} catch (Exception e) {
			throw new RuntimeException("담당자 정보 조회 중 오류가 발생했습니다.", e);
		}
	}

	// 담당자 정보 저장 (등록 또는 수정)
	public void saveManagerInfo(CompanyManagerVo managerVo, int memberId) {
		try {
			// 기업 정보 조회
			CompanyVo companyVo = companyPageDao.getCompanyByMemberId(memberId);
			if (companyVo == null) {
				throw new RuntimeException("기업 정보가 없습니다. 먼저 기업 정보를 등록해주세요.");
			}

			// 기업 ID 설정
			managerVo.setCompanyId(companyVo.getCompanyId());

			// 기존 담당자 정보 조회
			CompanyManagerVo existingManager = companyPageDao.getManagerByCompanyId(companyVo.getCompanyId());

			if (existingManager != null) {
				// 이미 담당자 정보가 있으면 ID 설정 후 업데이트
				managerVo.setManagerId(existingManager.getManagerId());
				companyPageDao.updateManager(managerVo);
			} else {
				// 담당자 정보가 없으면 새로 등록
				companyPageDao.insertManager(managerVo);
			}
		} catch (Exception e) {
			throw new RuntimeException("담당자 정보 저장 중 오류가 발생했습니다.", e);
		}
	}

	// 담당자 정보 수정 (updateManager 메서드는 더 이상 사용하지 않음)
	public void updateManager(CompanyManagerVo managerVo, int memberId) {
		// saveManagerInfo 메서드와 동일한 기능이므로 그대로 호출만 함
		saveManagerInfo(managerVo, memberId);
	}

}
