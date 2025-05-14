package com.javaex.idea.service;

import com.javaex.idea.dto.DisabledJobseekerDTO;
import com.javaex.config.ApiKeyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.javaex.idea.repository.DisabledJobseekerRepository;
import java.util.HashMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisabledJobseekerService {

    private final WebClient webClient;
    private final ApiKeyConfig apiKeyConfig;
    private final ObjectMapper objectMapper;
    private final DisabledJobseekerRepository repository;
    private final MongoTemplate mongoTemplate;

    // 공식 문서에 제공된 API URL 정보
    private static final String API_URL = "https://api.odcloud.kr/api/15014774/v1/uddi:bed031bf-2d7b-40ee-abef-b8e8ea0b0467";
    private static final int MAX_FETCH_SIZE = 1000; // 한 번에 가져올 데이터 수

    // 서비스 초기화 시 인덱스 생성
    @PostConstruct
    public void init() {
        createIndexes();
    }
    
    // MongoDB 인덱스 생성
    private void createIndexes() {
        try {
            log.info("MongoDB 인덱스 생성 시작");
            
            // 기본 ID 필드 인덱스
            mongoTemplate.indexOps(DisabledJobseekerDTO.class)
                .ensureIndex(new Index().on("id", org.springframework.data.domain.Sort.Direction.ASC).named("id_idx"));
            
            // 장애유형 인덱스
            mongoTemplate.indexOps(DisabledJobseekerDTO.class)
                .ensureIndex(new Index().on("장애유형", org.springframework.data.domain.Sort.Direction.ASC).named("disability_type_idx"));
            
            // 희망직종 인덱스
            mongoTemplate.indexOps(DisabledJobseekerDTO.class)
                .ensureIndex(new Index().on("희망직종", org.springframework.data.domain.Sort.Direction.ASC).named("job_type_idx"));
            
            // 희망지역 인덱스
            mongoTemplate.indexOps(DisabledJobseekerDTO.class)
                .ensureIndex(new Index().on("희망지역", org.springframework.data.domain.Sort.Direction.ASC).named("region_idx"));
                
            // 연번 인덱스
            mongoTemplate.indexOps(DisabledJobseekerDTO.class)
                .ensureIndex(new Index().on("연번", org.springframework.data.domain.Sort.Direction.ASC).named("seq_no_idx"));
            
            log.info("MongoDB 인덱스 생성 완료");
        } catch (Exception e) {
            log.error("MongoDB 인덱스 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    // 데이터를 가져와 저장
    public Mono<List<DisabledJobseekerDTO>> fetchAndSaveData() {
        // 몽고DB 컬렉션이 존재하는지 확인 및 생성
        createCollectionIfNotExists();
        
        // 마지막 저장된 연번 확인
        Optional<Integer> lastSequenceNo = getLastSequenceNumber();
        int startPage = calculateStartPage(lastSequenceNo.orElse(0));
        
        log.info("마지막 저장된 연번: {}, 시작 페이지: {}", lastSequenceNo.orElse(0), startPage);
        
        return fetchDataFromPage(startPage, MAX_FETCH_SIZE);
    }
    
    // 마지막 저장된 연번 확인
    private Optional<Integer> getLastSequenceNumber() {
        List<DisabledJobseekerDTO> existingData = repository.findAll();
        if (existingData.isEmpty()) {
            return Optional.of(0);
        }
        
        // 현재 저장된 최대 연번 찾기
        return existingData.stream()
                .map(DisabledJobseekerDTO::get연번)
                .max(Integer::compareTo);
    }
    
    // 시작 페이지 계산
    private int calculateStartPage(int lastSequenceNo) {
        // 이미 데이터가 있다면 마지막 연번 기준으로 다음 페이지부터 시작
        if (lastSequenceNo > 0) {
            return (lastSequenceNo / MAX_FETCH_SIZE) + 1;
        }
        // 데이터가 없으면 첫 페이지부터 시작
        return 1;
    }
    
    // 특정 페이지부터 데이터 가져오기
    private Mono<List<DisabledJobseekerDTO>> fetchDataFromPage(int page, int perPage) {
        log.info("페이지 {} 에서 {} 개의 데이터 가져오기 시도", page, perPage);
        
        String url = API_URL
            + "?serviceKey=" + apiKeyConfig.getEncodedKey()
            + "&page=" + page
            + "&perPage=" + perPage;
        
        log.info("공공데이터포털 API 호출: {}", url);
        
        return webClient.get()
                .uri(URI.create(url))
                .header("accept", "application/json")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(body -> log.debug("API 응답 본문: {}", body))
                .map(body -> {
                    try {
                        Map<String, Object> response = objectMapper.readValue(body, Map.class);
                        log.info("API 응답 구조: {}", response.keySet());
                        
                        // 데이터 추출 (공공데이터포털 오픈API 응답 구조)
                        List<Map<String, Object>> dataItems = (List<Map<String, Object>>) response.get("data");
                        if (dataItems == null || dataItems.isEmpty()) {
                            log.warn("API 응답에 'data' 키가 없거나 비어있습니다");
                            return Collections.<DisabledJobseekerDTO>emptyList();
                        }
                        
                        // 총 데이터 수 확인
                        int totalCount = ((Number) response.get("totalCount")).intValue();
                        log.info("API에서 가져온 데이터 수: {}, 총 데이터 수: {}", dataItems.size(), totalCount);
                        
                        // 항목 데이터를 DTO로 변환
                        List<DisabledJobseekerDTO> dtoList = new ArrayList<>();
                        for (Map<String, Object> item : dataItems) {
                            try {
                                DisabledJobseekerDTO dto = new DisabledJobseekerDTO();
                                
                                // 필드 매핑 (API 응답 필드 -> DTO 필드)
                                dto.set연번(parseIntSafely(item.get("연번")));
                                dto.set구직등록일(String.valueOf(item.get("구직등록일")));
                                dto.set기관분류(String.valueOf(item.get("기관분류")));
                                dto.set연령(parseIntSafely(item.get("연령")));
                                dto.set장애유형(String.valueOf(item.get("장애유형")));
                                dto.set중증여부(String.valueOf(item.get("중증여부")));
                                dto.set희망임금(String.valueOf(item.get("희망임금")));
                                dto.set희망지역(String.valueOf(item.get("희망지역")));
                                dto.set희망직종(String.valueOf(item.get("희망직종")));
                                
                                // ID 설정
                                dto.setId(String.valueOf(dto.get연번()));
                                
                                dtoList.add(dto);
                            } catch (Exception e) {
                                log.error("항목 변환 중 오류: {}", e.getMessage(), e);
                            }
                        }
                        
                        log.info("총 {}개의 구직자 데이터를 변환했습니다", dtoList.size());
                        
                        // 추가 정보 저장
                        return new FetchResult(dtoList, totalCount);
                    } catch (Exception e) {
                        log.error("API 응답 처리 중 오류: {}", e.getMessage(), e);
                        throw new RuntimeException("API 응답 데이터 처리 중 오류 발생", e);
                    }
                })
                .flatMap(result -> {
                    FetchResult fetchResult = (FetchResult) result;
                    
                    if (fetchResult.getData().isEmpty()) {
                        log.warn("API에서 가져온 데이터가 없습니다.");
                        return Mono.just(Collections.<DisabledJobseekerDTO>emptyList());
                    } else {
                        // 기존 데이터와 비교하여 중복 필터링
                        List<String> newDataIds = fetchResult.getData().stream()
                                .map(DisabledJobseekerDTO::getId)
                                .collect(Collectors.toList());
                        
                        // 기존 DB에서 이 ID들이 있는지 확인
                        List<DisabledJobseekerDTO> existingData = repository.findByIdIn(newDataIds);
                        
                        // 기존 데이터의 ID 집합
                        Set<String> existingIds = existingData.stream()
                                .map(DisabledJobseekerDTO::getId)
                                .collect(Collectors.toSet());
                        
                        // 신규 데이터만 필터링
                        List<DisabledJobseekerDTO> newDataOnly = fetchResult.getData().stream()
                                .filter(dto -> !existingIds.contains(dto.getId()))
                                .collect(Collectors.toList());
                        
                        if (!newDataOnly.isEmpty()) {
                            // 신규 데이터만 저장
                            repository.saveAll(newDataOnly);
                            log.info("{}개의 신규 데이터를 MongoDB에 저장했습니다", newDataOnly.size());
                        } else {
                            log.info("페이지 {}의 모든 데이터가 이미 DB에 존재합니다", page);
                        }
                        
                        // 마지막 페이지인지 확인
                        boolean hasMorePages = hasMorePages(page, perPage, fetchResult.getTotalCount());
                        
                        if (hasMorePages) {
                            log.info("다음 페이지가 있습니다. 페이지 {} 조회 중...", page + 1);
                            // 다음 페이지 데이터 가져오기 (재귀 호출)
                            return fetchDataFromPage(page + 1, perPage)
                                    .map(nextPageData -> {
                                        List<DisabledJobseekerDTO> combinedList = new ArrayList<>(newDataOnly);
                                        combinedList.addAll(nextPageData);
                                        return combinedList;
                                    });
                        }
                        
                        return Mono.just(newDataOnly);
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("API 요청 오류: {}, 상태 코드: {}, 응답 본문: {}", 
                            e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
                    // API 호출 실패 시 빈 리스트 반환
                    return Mono.just(Collections.<DisabledJobseekerDTO>emptyList());
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("API 처리 중 예외 발생: {}", e.getMessage(), e);
                    // 예외 발생 시 빈 리스트 반환
                    return Mono.just(Collections.<DisabledJobseekerDTO>emptyList());
                });
    }
    
    // 더 페이지가 있는지 확인
    private boolean hasMorePages(int currentPage, int perPage, int totalCount) {
        int maxPages = (int) Math.ceil((double) totalCount / perPage);
        return currentPage < maxPages;
    }
    
    // API 결과 저장 내부 클래스
    private static class FetchResult {
        private final List<DisabledJobseekerDTO> data;
        private final int totalCount;
        
        public FetchResult(List<DisabledJobseekerDTO> data, int totalCount) {
            this.data = data;
            this.totalCount = totalCount;
        }
        
        public List<DisabledJobseekerDTO> getData() {
            return data;
        }
        
        public int getTotalCount() {
            return totalCount;
        }
    }
    
    // 컬렉션 생성 확인
    private void createCollectionIfNotExists() {
        try {
            // 더미 문서 생성 후 삭제하여 컬렉션 존재 보장
            DisabledJobseekerDTO dummy = new DisabledJobseekerDTO();
            dummy.setId("temp_" + System.currentTimeMillis());
            repository.save(dummy);
            repository.deleteById(dummy.getId());
            log.info("'disabled_jobseekers' 컬렉션 확인 완료");
        } catch (Exception e) {
            log.error("컬렉션 생성 중 오류 발생: {}", e.getMessage());
        }
    }

    // DB에서 조회
    public List<DisabledJobseekerDTO> findAll() {
        return repository.findAll();
    }
    
    // 페이징 처리된 데이터 조회
    public Map<String, Object> findPaged(int page, int size) {
        return findPaged(page, size, null, null);
    }
    
    // 페이징 처리 및 필터링된 데이터 조회
    public Map<String, Object> findPaged(int page, int size, String search, List<String> disabilityTypes) {
        log.info("페이징 처리된 장애인 구직자 데이터 조회 - 페이지: {}, 크기: {}, 검색어: {}, 장애유형: {}", 
                 page, size, search, disabilityTypes);
        
        // 페이지 인덱스는 0부터 시작
        int pageIndex = page - 1;
        if (pageIndex < 0) pageIndex = 0;
        
        try {
            // MongoDB에서 바로 페이징 처리된 데이터를 가져오기
            List<DisabledJobseekerDTO> pagedData;
            int totalItems;
            
            if ((search == null || search.trim().isEmpty()) && (disabilityTypes == null || disabilityTypes.isEmpty())) {
                // 필터링이 없는 경우 - MongoDB 페이징 사용
                Pageable pageable = PageRequest.of(pageIndex, size);
                Page<DisabledJobseekerDTO> dataPage = repository.findAll(pageable);
                pagedData = dataPage.getContent();
                totalItems = (int) dataPage.getTotalElements();
                log.info("MongoDB 페이징을 사용하여 데이터 조회 완료 - 결과 수: {}, 총 항목 수: {}", pagedData.size(), totalItems);
            } else {
                // 필터링이 필요한 경우
                log.info("필터링이 필요한 조회 수행");
                
                // 필터 조건 구성
                List<DisabledJobseekerDTO> filteredData;
                
                if (search != null && !search.trim().isEmpty()) {
                    // 검색어 필터링 - MongoDB의 텍스트 검색 활용
                    String searchLower = search.toLowerCase();
                    if (disabilityTypes != null && !disabilityTypes.isEmpty()) {
                        // 검색어와 장애유형 모두 필터링
                        filteredData = repository.findBySearchTextAndDisabilityTypes(searchLower, disabilityTypes);
                        log.info("검색어와 장애유형으로 필터링된 데이터 수: {}", filteredData.size());
                    } else {
                        // 검색어로만 필터링
                        filteredData = repository.findBySearchText(searchLower);
                        log.info("검색어로 필터링된 데이터 수: {}", filteredData.size());
                    }
                } else if (disabilityTypes != null && !disabilityTypes.isEmpty()) {
                    // 장애유형으로만 필터링
                    filteredData = repository.findByDisabilityTypes(disabilityTypes);
                    log.info("장애유형으로 필터링된 데이터 수: {}", filteredData.size());
                } else {
                    // 필터링 조건이 없는 경우 (여기에 도달하지 않아야 함)
                    filteredData = Collections.emptyList();
                }
                
                totalItems = filteredData.size();
                
                // 메모리에서 페이징 처리
                int fromIndex = pageIndex * size;
                int toIndex = Math.min(fromIndex + size, totalItems);
                
                // 페이지 범위 검증
                if (fromIndex >= totalItems) {
                    if (totalItems > 0) {
                        fromIndex = (totalItems / size) * size;
                        toIndex = totalItems;
                    } else {
                        fromIndex = 0;
                        toIndex = 0;
                    }
                }
                
                pagedData = (fromIndex < toIndex) ? filteredData.subList(fromIndex, toIndex) : Collections.emptyList();
                log.info("메모리 내 페이징을 사용하여 필터링된 데이터 조회 완료 - 결과 수: {}", pagedData.size());
            }
            
            // 전체 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalItems / size);
            
            // 결과 맵 구성
            Map<String, Object> result = new HashMap<>();
            result.put("content", pagedData);
            result.put("totalItems", totalItems);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            
            return result;
        } catch (Exception e) {
            log.error("페이징 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            
            // 오류 발생 시 빈 결과 반환
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("content", Collections.emptyList());
            errorResult.put("totalItems", 0);
            errorResult.put("totalPages", 0);
            errorResult.put("currentPage", page);
            errorResult.put("error", "데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            
            return errorResult;
        }
    }
    
    // ID로 특정 구직자 조회
    public Optional<DisabledJobseekerDTO> findById(String id) {
        return repository.findById(id);
    }
    
    // 장애유형 목록 가져오기
    public List<String> getDisabilityTypes() {
        try {
            // MongoDB의 distinct 쿼리를 사용하여 고유한 장애유형 목록 가져오기
            List<String> types = mongoTemplate.getCollection("disabled_jobseekers")
                .distinct("장애유형", String.class)
                .into(new ArrayList<>());
            
            // null과 빈 문자열 제거 후 정렬
            return types.stream()
                .filter(type -> type != null && !type.trim().isEmpty())
                .sorted()
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("장애유형 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    // 데이터베이스 초기화 (테스트용)
    public void clearAll() {
        repository.deleteAll();
        log.info("disabled_jobseekers 컬렉션의 모든 데이터가 삭제되었습니다.");
    }
    
    // 테스트용 메서드 추가
    public String getServiceStatus() {
        return "정상 작동 중";
    }

    // 정수 파싱 안전하게 처리
    private int parseIntSafely(Object value) {
        if (value == null) return 0;
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
