package com.javaex.idea.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.javaex.idea.dao.SignupDao;
import com.javaex.idea.vo.SignupVo;

@Service
public class SignupService {
	
	@Autowired
	private SignupDao signupDao;
	@Autowired
	private S3Service s3Service;
	
	//중복체크
	public int exeCheckId(String id) {
		
		int idCheckResult = signupDao.checkId(id);
		return idCheckResult;
	};
	
	//회원가입
	public int exeSignup(Map<String, Object> signupDataMap) {
		
		Object dataVoObj = signupDataMap.get("dataVo");
		Map<String, String> dataVo = null;
		if (dataVoObj instanceof Map<?, ?> map) {
		    dataVo = map.entrySet().stream()
		        .collect(Collectors.toMap(
		            e -> String.valueOf(e.getKey()),
		            e -> String.valueOf(e.getValue())
		        ));
		}
		if (dataVo == null) {
		    throw new IllegalArgumentException("dataVo가 null입니다.");
		}
		MultipartFile disabilityImageURL = (MultipartFile) signupDataMap.get("disabilityImageURL");
		MultipartFile businessLicenseImg = (MultipartFile) signupDataMap.get("businessLicenseImg");
		
		// 1.member테이블에 등록(+xml에서 등록된 memberId 가져오기)
		SignupVo memberVo = new SignupVo();
		
		memberVo.setId(dataVo.get("id"));
		memberVo.setPw(dataVo.get("pw"));
		memberVo.setRole(dataVo.get("role"));
		memberVo.setState(1);
		
		int result = signupDao.memberSignup(memberVo);
		
		//member insert성공시
		if(result > 0) {
			// 2.role 꺼낸뒤, user와 company 나누기
			String role = dataVo.get("role");
			
			// -user
			if(role.equals("user")) {
				
				// user_01 장애유형 > 장애번호 가져오기
				String disabilityType = dataVo.get("disabilityType");
				int disabilityId = 0;
				
				if(disabilityType != null && !disabilityType.equals("")){
					disabilityId = signupDao.selectDisabilityId(disabilityType);
				}else {
					disabilityId = 5;
				};
				
				// user_02 (장애정보 + member정보 + user정보) user테이블에 insert
				SignupVo userVo = new SignupVo();
				
				userVo.setMemberId(memberVo.getMemberId());
				userVo.setName(dataVo.get("name"));
				userVo.setPhoneNumber(dataVo.get("ph"));
				userVo.setEmail(dataVo.get("email"));
				userVo.setDisabilityId(disabilityId);
				
				try {
					if(disabilityImageURL != null && !disabilityImageURL.isEmpty()) {
						String disabilityKey = "images/" + UUID.randomUUID() + "_" + disabilityImageURL.getOriginalFilename();
			            String disabilityURL = s3Service.uploadFile(disabilityKey, disabilityImageURL.getInputStream(),
			            		disabilityImageURL.getSize(), disabilityImageURL.getContentType());
			            
			            userVo.setDisabilityURL(disabilityURL);
					}
				}catch (IOException e) {
			        throw new RuntimeException("장애인등록증 업로드 중 오류가 발생했습니다.", e);
			    };
			    
				int userResult = signupDao.userSignup(userVo);
				return userResult;
				
			 // -company
			}else if(role.equals("company")) {
				
				SignupVo companyVo = new SignupVo();
				
				companyVo.setMemberId(memberVo.getMemberId());
				companyVo.setBusinessNumber(dataVo.get("businessNumber"));
				companyVo.setCompanyName(dataVo.get("companyName"));
				
				try {
					if(businessLicenseImg != null && !businessLicenseImg.isEmpty()) {
						String disabilityKey = "images/" + UUID.randomUUID() + "_" + businessLicenseImg.getOriginalFilename();
			            String businessLicense = s3Service.uploadFile(disabilityKey, businessLicenseImg.getInputStream(),
			            		businessLicenseImg.getSize(), businessLicenseImg.getContentType());
			            
			            companyVo.setBusinessLicense(businessLicense);
			            int companyResult = signupDao.companySignup(companyVo);
			            return companyResult;
					}
				}catch (IOException e) {
			        throw new RuntimeException("사업자등록증 업로드 중 오류가 발생했습니다.", e);
			    };
				
			};
			
		};
		
		return 0;
	}; //exeSignup
	
	//로그인
	public SignupVo exeLogin(SignupVo loginVo) {
		SignupVo authUser = signupDao.login(loginVo);
		return authUser;
	};
	
	//로그인시 멤버 정보가져오기
	public SignupVo exeGetMemberData(int memberId) {
		
		// 1.role 가져오기
		String role = signupDao.getRole(memberId);
		
		if(role.equals("user")) {
			
			SignupVo userVo = new SignupVo();
			
			String name = signupDao.getUser(memberId);
			
			userVo.setName(name);
			userVo.setRole(role);
			
			return userVo;
			
		}else if(role.equals("company")) {
			
			SignupVo companyVo = new SignupVo();
			
			String name = signupDao.getCompany(memberId);
			
			companyVo.setName(name);
			companyVo.setRole(role);
			
			return companyVo;
			
		}else {
			System.out.println("role정보가 없습니다.");
			return null;
		}
	};

	
	
	
	
};
