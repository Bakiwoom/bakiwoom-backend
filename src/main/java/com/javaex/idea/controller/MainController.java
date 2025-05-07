package com.javaex.idea.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javaex.idea.service.MainService;
import com.javaex.idea.vo.BookmarkVo;
import com.javaex.idea.vo.JobPostingVo;
import com.javaex.util.JsonResult;
import com.javaex.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/main")
public class MainController {

	@Autowired
	private MainService mainService;

	// 맞춤 공고 조회 (인증 필요)
	@GetMapping("/recommended")
	public JsonResult getRecommendedJobs(HttpServletRequest request) {
		try {
			// JWT 토큰에서 사용자 ID 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);

			List<JobPostingVo> jobs = mainService.getRecommendedJobs(memberId);

			if (jobs == null || jobs.isEmpty()) {
				return JsonResult.success(new ArrayList<>()); // 빈 리스트 반환
			}

			return JsonResult.success(jobs);

		} catch (Exception e) {
			return JsonResult.fail("맞춤 공고 조회 중 오류가 발생했습니다.");
		}
	}

	// 인기 공고 조회 (인증 불필요)
	@GetMapping("/popular")
	public JsonResult getPopularJobs() {
		try {
			List<JobPostingVo> jobs = mainService.getPopularJobs();

			if (jobs == null || jobs.isEmpty()) {
				return JsonResult.success(new ArrayList<>()); // 빈 리스트 반환
			}

			return JsonResult.success(jobs);

		} catch (Exception e) {
			return JsonResult.fail("인기 공고 조회 중 오류가 발생했습니다.");
		}
	}

	// 주목받는 공고 조회 (인증 불필요)
	@GetMapping("/trending")
	public JsonResult getTrendingJobs() {
		try {
			List<JobPostingVo> jobs = mainService.getTrendingJobs();

			if (jobs == null || jobs.isEmpty()) {
				return JsonResult.success(new ArrayList<>()); // 빈 리스트 반환
			}

			return JsonResult.success(jobs);

		} catch (Exception e) {
			return JsonResult.fail("주목받는 공고 조회 중 오류가 발생했습니다.");
		}
	}

	// 북마크 추가
	@PostMapping("/bookmarks")
	public JsonResult addBookmark(@RequestBody BookmarkVo bookmarkVo, HttpServletRequest request) {
		try {
			// JWT 토큰에서 member_id 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);

			// member_id를 사용해 user_id 조회
			Integer userId = mainService.getUserIdByMemberId(memberId);
			if (userId == null) {
				return JsonResult.fail("사용자 정보를 찾을 수 없습니다.");
			}

			// 디버깅 로그 추가
			System.out.println(
					"북마크 추가 - memberId: " + memberId + ", userId: " + userId + ", jobId: " + bookmarkVo.getJobId());

			bookmarkVo.setUserId(userId); // memberId가 아닌 userId 설정
			boolean result = mainService.addBookmark(bookmarkVo);

			if (!result) {
				return JsonResult.fail("이미 북마크된 공고입니다.");
			}

			return JsonResult.success(null);

		} catch (Exception e) {
			e.printStackTrace(); // 오류 스택트레이스 출력
			return JsonResult.fail("북마크 추가 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 북마크 삭제
	@DeleteMapping("/bookmarks/{jobId}")
	public JsonResult removeBookmark(@PathVariable int jobId, HttpServletRequest request) {
		try {
			// JWT 토큰에서 member_id 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);

			// member_id를 사용해 user_id 조회
			Integer userId = mainService.getUserIdByMemberId(memberId);
			if (userId == null) {
				return JsonResult.fail("사용자 정보를 찾을 수 없습니다.");
			}

			// 디버깅 로그 추가
			System.out.println("북마크 삭제 - memberId: " + memberId + ", userId: " + userId + ", jobId: " + jobId);

			boolean result = mainService.removeBookmark(userId, jobId);

			if (!result) {
				return JsonResult.fail("북마크 삭제에 실패했습니다.");
			}

			return JsonResult.success(null);

		} catch (Exception e) {
			e.printStackTrace(); // 오류 스택트레이스 출력
			return JsonResult.fail("북마크 삭제 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 사용자의 북마크 목록 조회
	@GetMapping("/bookmarks/user")
	public JsonResult getUserBookmarks(HttpServletRequest request) {
		try {
			// JWT 토큰에서 member_id 가져오기
			Integer memberId = JwtUtil.getNoFromHeader(request);

			// member_id를 사용해 user_id 조회
			Integer userId = mainService.getUserIdByMemberId(memberId);
			if (userId == null) {
				return JsonResult.fail("사용자 정보를 찾을 수 없습니다.");
			}

			// 디버깅 로그 추가
			System.out.println("북마크 목록 조회 - memberId: " + memberId + ", userId: " + userId);

			List<BookmarkVo> bookmarks = mainService.getUserBookmarks(userId);

			if (bookmarks == null) {
				return JsonResult.success(new ArrayList<>()); // 빈 리스트 반환
			}

			// 디버깅 로그 추가
			System.out.println("조회된 북마크 수: " + bookmarks.size());

			return JsonResult.success(bookmarks);

		} catch (Exception e) {
			e.printStackTrace(); // 오류 스택트레이스 출력
			return JsonResult.fail("북마크 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

}
