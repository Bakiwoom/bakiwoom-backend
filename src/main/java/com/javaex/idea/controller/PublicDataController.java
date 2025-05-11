package com.javaex.idea.controller;

import com.javaex.idea.dto.DisabledJobofferDTO;
import com.javaex.idea.service.DisabledJobofferService;
import com.javaex.idea.dto.WelfareServiceListDTO;
import com.javaex.idea.dto.WelfareServiceDetailDTO;
import com.javaex.idea.service.WelfareServiceService;
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

@Slf4j
@Tag(name = "Public Data API", description = "공공데이터 API 엔드포인트")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicDataController {
    
    private final DisabledJobofferService disabledJobofferService;
    private final WelfareServiceService welfareServiceService;
    
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
} 