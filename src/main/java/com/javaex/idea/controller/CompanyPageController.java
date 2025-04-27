package com.javaex.idea.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.javaex.idea.service.S3Service;
import com.javaex.idea.service.CompanyPageService;
import com.javaex.idea.vo.CompanyVo;
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

	// 기업 정보 입력
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
			// int memberId = JwtUtil.getNoFromHeader(request);
			int memberId = 1; // (로그인 구현 전 임시값 사용)

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
	
	// 기업 정보 가져오기
	@GetMapping("/company/detail")
	public JsonResult getCompanyDetail(HttpServletRequest request) {
	    try {
	        // 1. JWT 토큰에서 사용자 ID 가져오기
	        //Integer memberId = JwtUtil.getNoFromHeader(request);
	    	int memberId = 1; // (로그인 구현 전 임시값 사용)

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

}
