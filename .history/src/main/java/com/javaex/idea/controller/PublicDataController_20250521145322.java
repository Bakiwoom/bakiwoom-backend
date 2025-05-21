package com.javaex.idea.controller;

import com.javaex.idea.dto.DisabledJobofferDTO;
import com.javaex.idea.service.DisabledJobofferService;
import com.javaex.idea.dto.WelfareServiceListDTO;
import com.javaex.idea.dto.WelfareServiceDetailDTO;
import com.javaex.idea.service.WelfareServiceService;
import com.javaex.idea.dto.DisabilityTypeStatsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Arrays;

import com.javaex.idea.dto.DisabledJobseekerDTO;
import com.javaex.idea.service.DisabledJobseekerService;
import com.javaex.idea.dto.RegionStatsDTO;

@Slf4j
@Tag(name = "Public Data API", description = "공공데이터 API 엔드포인트")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicDataController {
    
    private final DisabledJobofferService disabledJobofferService;
    private final WelfareServiceService welfareServiceService;
    private final DisabledJobseekerService disabledJobseekerService;
    
    @Operation(summary = "장애인 구인 실시간 현황 데이터 조회", description = "지역, 장애유형, 연령대별로 장애인 구인 실시간 현황을 조회합니다.")
    @GetMapping(value = "/disabled/job-offers", produces = "application/json")
    public ResponseEntity<List<DisabledJobofferDTO>> getDisabledJoboffers() {
        System.out.println("==== [Controller] getDisabledJoboffers 호출됨 ====");
        log.info("==== [Controller] getDisabledJoboffers 호출됨 ====");
        return ResponseEntity.ok(disabledJobofferService.findByFilters(null, null, null));
    }
    
    @Operation(summary = "장애인 구인 실시간 현황 데이터 갱신", description = "공공데이터 API에서 최신 장애인 구인 실시간 현황 데이터를 가져와 저장합니다.")
    @PostMapping("/disabled/job-offers/refresh")
    public Mono<ResponseEntity<List<DisabledJobofferDTO>>> refreshDisabledJoboffers() {
        return disabledJobofferService.fetchAndSaveData()
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "복지서비스 목록 조회", description = "중앙부처 복지서비스 목록을 조회합니다.")
    @GetMapping("/welfare/list")
    public ResponseEntity<List<WelfareServiceListDTO>> getWelfareList() {
        log.info("[Controller] getWelfareList 진입");
        List<WelfareServiceListDTO> list = welfareServiceService.getAllList();
        log.info("[Controller] getWelfareList 결과 개수: {}", list.size());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "복지서비스 키워드 검색", description = "키워드로 복지서비스를 검색합니다.")
    @GetMapping("/welfare/search")
    public ResponseEntity<List<WelfareServiceListDTO>> searchWelfareByKeywords(
            @RequestParam String keyword) {
        log.info("[Controller] searchWelfareByKeywords 진입 - keyword: {}", keyword);
        List<WelfareServiceListDTO> results = welfareServiceService.searchByKeywords(keyword);
        log.info("[Controller] searchWelfareByKeywords 결과 개수: {}", results.size());
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "복지서비스 상세 조회", description = "servId로 복지서비스 상세정보를 조회합니다.")
    @GetMapping("/welfare/detail/{servId}")
    public Mono<ResponseEntity<WelfareServiceDetailDTO>> getWelfareDetail(@PathVariable String servId) {
        log.info("[Controller] getWelfareDetail 진입 - servId: {}", servId);
        return welfareServiceService.getDetail(servId)
            .map(dto -> ResponseEntity.ok(dto))
            .onErrorResume(e -> {
                log.error("[Controller] getWelfareDetail 에러 - servId: {}", servId, e);
                return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body((WelfareServiceDetailDTO) null));
            });
    }

    @Operation(summary = "복지서비스 목록 갱신", description = "공공데이터 API에서 복지서비스 목록을 갱신합니다.")
    @PostMapping("/welfare/list/refresh")
    public Mono<ResponseEntity<List<WelfareServiceListDTO>>> refreshWelfareList() {
        log.info("[Controller] refreshWelfareList 진입");
        return welfareServiceService.fetchAndSaveAllList()
                .doOnSuccess(list -> log.info("[Controller] refreshWelfareList 저장 개수: {}", list.size()))
                .doOnError(e -> log.error("[Controller] refreshWelfareList 에러", e))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "복지서비스 상세 갱신", description = "공공데이터 API에서 복지서비스 상세정보를 갱신합니다.")
    @PostMapping("/welfare/detail/{servId}/refresh")
    public Mono<ResponseEntity<?>> refreshWelfareDetail(@PathVariable String servId) {
        log.info("[Controller] refreshWelfareDetail 진입 - servId: {}", servId);
        return welfareServiceService.fetchAndSaveDetail(servId)
                .doOnSuccess(detail -> log.info("[Controller] refreshWelfareDetail 저장 결과: {}", detail != null ? "성공" : "실패"))
                .doOnError(e -> log.error("[Controller] refreshWelfareDetail 에러", e))
                .map(detail -> {
                    if (detail == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Collections.singletonMap("error", "상세 정보를 찾을 수 없습니다."));
                    }
                    return ResponseEntity.ok(detail);
                });
    }

    @Operation(summary = "장애인 구직자 현황 데이터 갱신", description = "한국장애인고용공단 장애인 구직자 현황 데이터를 가져와 저장합니다. 이전 데이터는 유지됩니다.")
    @PostMapping("/disabled/jobseekers/refresh")
    public Mono<ResponseEntity<List<DisabledJobseekerDTO>>> refreshDisabledJobseekers() {
        log.info("장애인 구직자 현황 데이터 갱신 요청");
        
        return disabledJobseekerService.fetchAndSaveData()
                .map(data -> {
                    log.info("장애인 구직자 현황 데이터 갱신 성공 - 새로 추가된 데이터 수: {}", data.size());
                    return ResponseEntity.ok(data);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "장애인 구직자 현황 데이터 조회", description = "저장된 장애인 구직자 현황 데이터를 조회합니다.")
    @GetMapping("/disabled/jobseekers")
    public ResponseEntity<Map<String, Object>> getDisabledJobseekers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String disabilityTypes) {
        log.info("장애인 구직자 현황 데이터 조회 요청 - 페이지: {}, 크기: {}, 검색어: {}, 장애유형: {}", 
                 page, size, search, disabilityTypes);
        
        // 쉼표로 구분된 장애유형 문자열을 리스트로 변환
        List<String> disabilityTypeList = null;
        if (disabilityTypes != null && !disabilityTypes.isEmpty()) {
            disabilityTypeList = Arrays.asList(disabilityTypes.split(","));
            log.info("장애유형 변환 결과: {}", disabilityTypeList);
        }
        
        Map<String, Object> response = disabledJobseekerService.findPaged(page, size, search, disabilityTypeList);
        log.info("장애인 구직자 현황 데이터 조회 결과 - 페이지: {}, 데이터 수: {}", page, 
                ((List<DisabledJobseekerDTO>)response.get("content")).size());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "장애인 구직자 상세 조회", description = "ID로 특정 장애인 구직자 정보를 조회합니다.")
    @GetMapping("/disabled/jobseekers/{id}")
    public ResponseEntity<?> getDisabledJobseekerById(@PathVariable String id) {
        log.info("장애인 구직자 상세 조회 요청 - ID: {}", id);
        Optional<DisabledJobseekerDTO> result = disabledJobseekerService.findById(id);
        
        if (result.isPresent()) {
            log.info("장애인 구직자 상세 조회 성공 - ID: {}", id);
            return ResponseEntity.ok(result.get());
        } else {
            log.warn("장애인 구직자 상세 조회 실패 - 해당 ID가 없음: {}", id);
            Map<String, String> errorResponse = Collections.singletonMap("error", "해당 ID의 구직자 정보가 없습니다: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @Operation(summary = "장애인 구직자 데이터 초기화", description = "모든 장애인 구직자 데이터를 삭제합니다. (테스트용)")
    @DeleteMapping("/disabled/jobseekers/all")
    public ResponseEntity<?> clearDisabledJobseekers() {
        log.info("장애인 구직자 데이터 초기화 요청");
        disabledJobseekerService.clearAll();
        return ResponseEntity.ok(Collections.singletonMap("message", "모든 장애인 구직자 데이터가 삭제되었습니다."));
    }

    @Operation(summary = "장애인 구직자 장애유형 목록 조회", description = "등록된 모든 장애유형 목록을 조회합니다.")
    @GetMapping("/disabled/jobseekers/disability-types")
    public ResponseEntity<List<String>> getDisabilityTypes() {
        log.info("장애인 구직자 장애유형 목록 조회 요청");
        List<String> types = disabledJobseekerService.getDisabilityTypes();
        log.info("장애유형 목록 조회 결과: {} 개", types.size());
        return ResponseEntity.ok(types);
    }

    @Operation(summary = "장애유형별 통계 조회", description = "특정 장애유형에 대한 상세 통계 정보를 조회합니다.")
    @GetMapping("/disabled/jobseekers/stats/{disabilityType}")
    public ResponseEntity<DisabilityTypeStatsDTO> getDisabilityTypeStats(@PathVariable String disabilityType) {
        log.info("장애유형별 통계 조회 요청 - 장애유형: {}", disabilityType);
        
        try {
            DisabilityTypeStatsDTO stats = disabledJobseekerService.getDisabilityTypeStats(disabilityType);
            log.info("장애유형별 통계 조회 성공 - 장애유형: {}", disabilityType);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.warn("장애유형별 통계 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("장애유형별 통계 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "지역별 구직자 통계 조회", description = "특정 지역의 장애인 구직자 통계 정보를 조회합니다.")
    @GetMapping("/disabled/jobseekers/stats/region/{region}")
    public ResponseEntity<RegionStatsDTO> getRegionStats(@PathVariable String region) {
        log.info("지역별 구직자 통계 조회 요청 - 지역: {}", region);
        
        try {
            RegionStatsDTO stats = disabledJobseekerService.getRegionStats(region);
            log.info("지역별 구직자 통계 조회 성공 - 지역: {}", region);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.warn("지역별 구직자 통계 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("지역별 구직자 통계 조회 실패 - 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 