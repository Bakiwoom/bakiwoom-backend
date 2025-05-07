package com.javaex.idea.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.javaex.idea.service.S3Service;
import com.javaex.idea.service.CompanyPageService;
import com.javaex.idea.vo.ApplicationVo;
import com.javaex.idea.vo.CompanyManagerVo;
import com.javaex.idea.vo.CompanyVo;
import com.javaex.idea.vo.MemberVo;
import com.javaex.util.JsonResult;
import com.javaex.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/companies/me")
public class CompanyPageController {

	@Autowired
	private CompanyPageService companyPageService;

	@Autowired
	private S3Service s3Service;

	// 기업 정보 조회
	@GetMapping("/company/detail")
	public JsonResult getCompanyDetail(HttpServletRequest request) {
		try {
			// 1. JWT 토큰에서 사용자 ID 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);
			//int memberId = 1; // (로그인 구현 전 임시값 사용)

			// 2. 회사 상세 정보 가져오기
			CompanyVo companyDetail = companyPageService.getCompanyDetail(memberId);

			if (companyDetail == null) {
				return JsonResult.fail("해당 회사 정보를 찾을 수 없습니다.");
			}

			return JsonResult.success(companyDetail);

		} catch (Exception e) {
			return JsonResult.fail("회사 상세 정보 조회 중 오류가 발생했습니다.");
		}
	}

	// 기업 정보 등록
	@PostMapping("/company/save")
	public JsonResult saveCompanyInfo(@RequestParam(value = "logo", required = false) MultipartFile logoFile,
			@RequestParam("name") String name, @RequestParam("businessNumber") String businessNumber,
			@RequestParam("businessType") String businessType, @RequestParam("size") String size,
			@RequestParam("foundingYear") String foundingYear, @RequestParam("employeeCount") int employeeCount,
			@RequestParam("address") String address, @RequestParam("phone") String phone,
			@RequestParam("email") String email, @RequestParam(value = "website", required = false) String website,
			@RequestParam(value = "intro", required = false) String intro, HttpServletRequest request) {

		try {
			// 1. 토큰에서 사용자 ID 추출
			int memberId = JwtUtil.getNoFromHeader(request);
			//int memberId = 1; // (로그인 구현 전 임시값 사용)

			// 2. CompanyVo 객체 생성 및 데이터 설정
			CompanyVo companyVo = new CompanyVo();
			companyVo.setName(name);
			companyVo.setBusinessNumber(businessNumber);
			companyVo.setBusinessType(businessType);
			companyVo.setSize(size);
			companyVo.setFoundingYear(foundingYear);
			companyVo.setEmployeeCount(employeeCount);
			companyVo.setAddress(address);
			companyVo.setPhone(phone);
			companyVo.setEmail(email);
			companyVo.setWebsite(website);
			companyVo.setIntro(intro);

			// 3. 로고 파일 처리
			if (logoFile != null && !logoFile.isEmpty()) {
				String key = "images/" + UUID.randomUUID() + "_" + logoFile.getOriginalFilename();
				String logoUrl = s3Service.uploadFile(key, logoFile.getInputStream(), logoFile.getSize(),
						logoFile.getContentType());
				companyVo.setLogo(logoUrl);
			}

			// 4. 서비스 호출하여 기업 정보 저장
			companyPageService.saveCompanyInfo(companyVo, memberId);

			return JsonResult.success("기업 정보가 성공적으로 저장되었습니다.");

		} catch (IOException e) {
			throw new RuntimeException("로고 파일 업로드 중 오류가 발생했습니다.", e);
		} catch (Exception e) {
			return JsonResult.fail("기업 정보 저장 중 오류가 발생했습니다.");
		}
	}

	// 기업 정보 수정
	@PostMapping("/company/update")
	public JsonResult updateCompanyInfo(HttpServletRequest request, @RequestParam("name") String name,
			@RequestParam("businessNumber") String businessNumber, @RequestParam("businessType") String businessType,
			@RequestParam("size") String size, @RequestParam("foundingYear") String foundingYear,
			@RequestParam("employeeCount") Integer employeeCount, @RequestParam("address") String address,
			@RequestParam("phone") String phone, @RequestParam("email") String email,
			@RequestParam(value = "website", required = false) String website,
			@RequestParam(value = "intro", required = false) String intro,
			@RequestParam(value = "logo", required = false) MultipartFile logo) {

		try {
			// 1. JWT 토큰에서 사용자 ID 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);
			//int memberId = 1; // (로그인 구현 전 임시값 사용)

			CompanyVo companyVo = new CompanyVo();
			companyVo.setName(name);
			companyVo.setBusinessNumber(businessNumber);
			companyVo.setBusinessType(businessType);
			companyVo.setSize(size);
			companyVo.setFoundingYear(foundingYear);
			companyVo.setEmployeeCount(employeeCount);
			companyVo.setAddress(address);
			companyVo.setPhone(phone);
			companyVo.setEmail(email);
			companyVo.setWebsite(website);
			companyVo.setIntro(intro);

			companyPageService.updateCompany(companyVo, memberId, logo);

			return JsonResult.success(null);

		} catch (Exception e) {
			return JsonResult.fail("회사 정보 수정 중 오류가 발생했습니다.");
		}
	}

	// 담당자 정보 조회
	@GetMapping("/manager/detail")
	public JsonResult getManagerDetail(HttpServletRequest request) {
		try {
			// 1. JWT 토큰에서 사용자 ID 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);
			//int memberId = 1; // (로그인 구현 전 임시값 사용)

			// 2. 담당자 정보 가져오기
			CompanyManagerVo managerDetail = companyPageService.getManagerDetail(memberId);

			if (managerDetail == null) {
				return JsonResult.fail("담당자 정보를 찾을 수 없습니다.");
			}

			return JsonResult.success(managerDetail);

		} catch (Exception e) {
			return JsonResult.fail("담당자 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 담당자 정보 등록
	@PostMapping("/manager/save")
	public JsonResult createManager(@RequestBody CompanyManagerVo managerVo, HttpServletRequest request) {
		try {
			// 1. JWT 토큰에서 사용자 ID 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);
			//int memberId = 1; // (로그인 구현 전 임시값 사용)

			// 2. 필수 필드 검증
			if (managerVo.getName() == null || managerVo.getName().trim().isEmpty() || managerVo.getPosition() == null
					|| managerVo.getPosition().trim().isEmpty() || managerVo.getPhone() == null
					|| managerVo.getPhone().trim().isEmpty() || managerVo.getEmail() == null
					|| managerVo.getEmail().trim().isEmpty()) {
				return JsonResult.fail("필수 항목(이름, 직책, 연락처, 이메일)을 모두 입력해주세요.");
			}

			// 3. 서비스 호출하여 담당자 정보 저장
			companyPageService.saveManagerInfo(managerVo, memberId);

			return JsonResult.success("담당자 정보가 성공적으로 등록되었습니다.");
		} catch (Exception e) {
			return JsonResult.fail("담당자 정보 등록 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 담당자 정보 수정
	@PostMapping("/manager/update")
	public JsonResult updateManager(@RequestBody CompanyManagerVo managerVo, HttpServletRequest request) {
		try {
			// 1. JWT 토큰에서 사용자 ID 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);
			//int memberId = 1; // (로그인 구현 전 임시값 사용)

			// 2. 필수 필드 검증
			if (managerVo.getName() == null || managerVo.getName().trim().isEmpty() || managerVo.getPosition() == null
					|| managerVo.getPosition().trim().isEmpty() || managerVo.getPhone() == null
					|| managerVo.getPhone().trim().isEmpty() || managerVo.getEmail() == null
					|| managerVo.getEmail().trim().isEmpty()) {
				return JsonResult.fail("필수 항목(이름, 직책, 연락처, 이메일)을 모두 입력해주세요.");
			}

			// 3. 서비스 호출하여 담당자 정보 수정
			companyPageService.saveManagerInfo(managerVo, memberId);

			return JsonResult.success("담당자 정보가 성공적으로 수정되었습니다.");

		} catch (Exception e) {
			return JsonResult.fail("담당자 정보 수정 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 계정 정보 조회
	@GetMapping("/account/detail")
	public JsonResult getAccountDetail(HttpServletRequest request) {
		try {
			// 1. JWT 토큰에서 사용자 ID 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);
			//int memberId = 1; // (로그인 구현 전 임시값 사용)

			// 2. 계정 정보 가져오기
			MemberVo accountDetail = companyPageService.getAccountDetail(memberId);

			if (accountDetail == null) {
				return JsonResult.fail("계정 정보를 찾을 수 없습니다.");
			}

			// 비밀번호 정보는 제외하고 반환
			accountDetail.setPassword(null);

			return JsonResult.success(accountDetail);

		} catch (Exception e) {
			return JsonResult.fail("계정 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 비밀번호 변경
	@PostMapping("/account/password")
	public JsonResult changePassword(@RequestBody Map<String, String> passwordData, HttpServletRequest request) {
		try {
			// 1. JWT 토큰에서 사용자 ID 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);
			//int memberId = 1; // (로그인 구현 전 임시값 사용)

			// 2. 필수 필드 검증
			String currentPassword = passwordData.get("currentPassword");
			String newPassword = passwordData.get("newPassword");

			if (currentPassword == null || currentPassword.trim().isEmpty() || newPassword == null
					|| newPassword.trim().isEmpty()) {
				return JsonResult.fail("현재 비밀번호와 새 비밀번호를 모두 입력해주세요.");
			}

			// 3. 서비스 호출하여 비밀번호 변경
			boolean result = companyPageService.changePassword(memberId, currentPassword, newPassword);

			if (!result) {
				return JsonResult.fail("현재 비밀번호가 일치하지 않습니다.");
			}

			return JsonResult.success("비밀번호가 성공적으로 변경되었습니다.");

		} catch (Exception e) {
			System.out.println("비밀번호 변경 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return JsonResult.fail("비밀번호 변경 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 회사의 지원 내역 조회
    @GetMapping("/applications")
    public JsonResult getApplications(HttpServletRequest request) {
        try {
            // 1. JWT 토큰에서 사용자 ID 가져오기
            Integer memberId = JwtUtil.getNoFromHeader(request);
            //int memberId = 1; // (로그인 구현 전 임시값 사용)
            
            // 2. 회원 ID로 회사 정보 조회
            CompanyVo companyVo = companyPageService.getCompanyDetail(memberId);
            
            if (companyVo == null) {
                return JsonResult.fail("회사 정보를 찾을 수 없습니다.");
            }
            
            // 3. 회사 ID로 지원 내역 조회
            List<ApplicationVo> applications = companyPageService.getApplicationsByCompanyId(companyVo.getCompanyId());
            
            return JsonResult.success(applications);
        } catch (Exception e) {
            return JsonResult.fail("지원 내역을 불러오는데 실패했습니다: " + e.getMessage());
        }
    }

}
