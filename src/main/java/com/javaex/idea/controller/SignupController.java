package com.javaex.idea.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.javaex.idea.service.SignupService;
import com.javaex.idea.vo.SignupVo;
import com.javaex.util.JsonResult;
import com.javaex.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class SignupController {
	
	@Autowired
	private SignupService signupService;
	
	//아이디 중복체크
	@GetMapping(value="/api/checkId")
	public JsonResult checkId(@RequestParam ("id") String id) {
		
		int idCheckResult = signupService.exeCheckId(id);
		
		if(idCheckResult == 0) {
			return JsonResult.success("인증성공");
		}else {
			return JsonResult.fail("중복ID");
		}
	};
	
	
	//회원가입
	@PostMapping(value="/api/signup")
	public JsonResult signup(@RequestParam Map<String, String> params
						,@RequestParam(value = "obstacle", required = false) MultipartFile disabilityImageURL
						,@RequestParam(value = "businessImg", required = false) MultipartFile businessLicenseImg) {
		
	    
	    Map<String, Object> signupDataMap = new HashMap<>();
	    signupDataMap.put("dataVo", params);
	    signupDataMap.put("disabilityImageURL", disabilityImageURL);
	    signupDataMap.put("businessLicenseImg", businessLicenseImg);
	    
	    // 장애인 등록증 파일처리
        if (disabilityImageURL != null && !disabilityImageURL.isEmpty()) {
            System.out.println("파일 이름: " + disabilityImageURL.getOriginalFilename());
        } else {
            System.out.println("장애인 증명서 파일이 업로드되지 않았습니다.");
        }
        
        // 사업자 등록증 파일 처리
        if (businessLicenseImg != null && !businessLicenseImg.isEmpty()) {
            System.out.println("사업자 등록증 파일 이름: " + businessLicenseImg.getOriginalFilename());
        } else {
            System.out.println("사업자 등록증 파일이 업로드되지 않았습니다.");
        }
        
        //서비스로 값 전달
        int result = signupService.exeSignup(signupDataMap);
		
        if(result > 0) {
        	return JsonResult.success("회원가입 성공");
        }else {
        	return JsonResult.fail("회원가입 실패");
        }
	};
	
	//로그인
	@PostMapping(value="/api/login")
	public JsonResult login(@RequestBody SignupVo loginVo, HttpServletResponse response) {
		SignupVo authUser = signupService.exeLogin(loginVo);
		if(authUser != null && authUser.getMemberId() != 0) {
			// JWT 토큰 생성 및 반환
			JwtUtil.createTokenAndSetHeader(response, "" + authUser.getMemberId());

            // userName 필드 추가 (일반회원: name, 기업회원: companyName)
            Map<String, Object> result = new HashMap<>();
            result.put("memberId", authUser.getMemberId());
            result.put("role", authUser.getRole());
            String userName = (authUser.getRole() != null && authUser.getRole().equals("company")) ? authUser.getCompanyName() : authUser.getName();
            result.put("userName", userName);
            // 필요시 기타 정보 추가

			return JsonResult.success(result);
		}else {
			return JsonResult.fail("로그인 실패");
		}
	};
	
	//로그인시 회원정보 가져오기
	@GetMapping(value="/api/member/data/{memberId}")
	public JsonResult getMemberData(@PathVariable ("memberId") int memberId) {
		SignupVo loginMemberVo = signupService.exeGetMemberData(memberId);
		
		if(loginMemberVo != null) {
			return JsonResult.success(loginMemberVo);
		}else {
			return JsonResult.fail("회원이름 가져오기 실패");
		}
	};
	
	

}
