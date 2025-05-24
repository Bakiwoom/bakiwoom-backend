package com.javaex.idea.service;

import com.javaex.idea.dto.DisabledJobseekerDTO;
import com.javaex.idea.dto.DisabilityTypeStatsDTO;
import com.javaex.idea.dto.RegionStatsDTO;
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
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
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

    // --- 집계 결과 매핑 DTO 정의 시작 ---
    public static class DistributionItemDTO {
        private Object _id;
        private long count;

        public Object get_id() { return _id; }
        public void set_id(Object _id) { this._id = _id; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class CombinedStatsIdDTO {
        private String severity;
        private String region;
        private String ageGroup;
        private String jobType;
        private String salary;

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getAgeGroup() { return ageGroup; }
        public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
        public String getJobType() { return jobType; }
        public void setJobType(String jobType) { this.jobType = jobType; }
        public String getSalary() { return salary; }
        public void setSalary(String salary) { this.salary = salary; }
    }

    public static class CombinedAggregationResultDTO {
        private CombinedStatsIdDTO _id;
        private long count;

        public CombinedStatsIdDTO get_id() { return _id; }
        public void set_id(CombinedStatsIdDTO _id) { this._id = _id; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
    // --- 집계 결과 매핑 DTO 정의 끝 ---

    // 서비스 초기화 시 인덱스 생성
    @PostConstruct
    public void init() {
        createIndexes();
    }
    
    // MongoDB 인덱스 생성
    private void createIndexes() {
        try {
            log.info("MongoDB 인덱스 생성 시작");
            
            mongoTemplate.indexOps(DisabledJobseekerDTO.class)
                .ensureIndex(new Index().on("id", org.springframework.data.domain.Sort.Direction.ASC).named("id_idx"));
            mongoTemplate.indexOps(DisabledJobseekerDTO.class)
                .ensureIndex(new Index().on("장애유형", org.springframework.data.domain.Sort.Direction.ASC).named("disability_type_idx"));
            mongoTemplate.indexOps(DisabledJobseekerDTO.class)
                .ensureIndex(new Index().on("희망직종", org.springframework.data.domain.Sort.Direction.ASC).named("job_type_idx"));
            mongoTemplate.indexOps(DisabledJobseekerDTO.class)
                .ensureIndex(new Index().on("희망지역", org.springframework.data.domain.Sort.Direction.ASC).named("region_idx"));
            mongoTemplate.indexOps(DisabledJobseekerDTO.class)
                .ensureIndex(new Index().on("연번", org.springframework.data.domain.Sort.Direction.ASC).named("seq_no_idx"));
            
            log.info("MongoDB 인덱스 생성 완료");
        } catch (Exception e) {
            log.error("MongoDB 인덱스 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public Mono<List<DisabledJobseekerDTO>> fetchAndSaveData() {
        createCollectionIfNotExists();
        Optional<Integer> lastSequenceNo = getLastSequenceNumber();
        int startPage = calculateStartPage(lastSequenceNo.orElse(0));
        log.info("마지막 저장된 연번: {}, 시작 페이지: {}", lastSequenceNo.orElse(0), startPage);
        return fetchDataFromPage(startPage, MAX_FETCH_SIZE);
    }
    
    private Optional<Integer> getLastSequenceNumber() {
        List<DisabledJobseekerDTO> existingData = repository.findAll();
        if (existingData.isEmpty()) {
            return Optional.of(0);
        }
        return existingData.stream()
                .map(DisabledJobseekerDTO::get연번)
                .max(Integer::compareTo);
    }
    
    private int calculateStartPage(int lastSequenceNo) {
        if (lastSequenceNo > 0) {
            return (lastSequenceNo / MAX_FETCH_SIZE) + 1;
        }
        return 1;
    }
    
    @SuppressWarnings("unchecked") // API 응답의 'data' 필드 캐스팅 관련
    private Mono<List<DisabledJobseekerDTO>> fetchDataFromPage(int page, int perPage) {
        log.info("페이지 {} 에서 {} 개의 데이터 가져오기 시도", page, perPage);
        String url = API_URL + "?serviceKey=" + apiKeyConfig.getEncodedKey() + "&page=" + page + "&perPage=" + perPage;
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
                        
                        List<Map<String, Object>> dataItems = (List<Map<String, Object>>) response.get("data");
                        if (dataItems == null || dataItems.isEmpty()) {
                            log.warn("API 응답에 'data' 키가 없거나 비어있습니다");
                            return new FetchResult(Collections.<DisabledJobseekerDTO>emptyList(), 0); // totalCount 0으로 수정
                        }
                        
                        int totalCountFromApi = response.containsKey("totalCount") ? ((Number) response.get("totalCount")).intValue() : 0;
                        log.info("API에서 가져온 데이터 수: {}, 총 데이터 수: {}", dataItems.size(), totalCountFromApi);
                        
                        List<DisabledJobseekerDTO> dtoList = new ArrayList<>();
                        for (Map<String, Object> item : dataItems) {
                            try {
                                DisabledJobseekerDTO dto = new DisabledJobseekerDTO();
                                dto.set연번(parseIntSafely(item.get("연번")));
                                dto.set구직등록일(String.valueOf(item.get("구직등록일")));
                                dto.set기관분류(String.valueOf(item.get("기관분류")));
                                dto.set연령(parseIntSafely(item.get("연령")));
                                dto.set장애유형(String.valueOf(item.get("장애유형")));
                                dto.set중증여부(String.valueOf(item.get("중증여부")));
                                dto.set희망임금(String.valueOf(item.get("희망임금")));
                                dto.set희망지역(String.valueOf(item.get("희망지역")));
                                dto.set희망직종(String.valueOf(item.get("희망직종")));
                                dto.setId(String.valueOf(dto.get연번()));
                                dtoList.add(dto);
                            } catch (Exception e) {
                                log.error("항목 변환 중 오류: {}", e.getMessage(), e);
                            }
                        }
                        log.info("총 {}개의 구직자 데이터를 변환했습니다", dtoList.size());
                        return new FetchResult(dtoList, totalCountFromApi);
                    } catch (Exception e) {
                        log.error("API 응답 처리 중 오류: {}", e.getMessage(), e);
                        throw new RuntimeException("API 응답 데이터 처리 중 오류 발생", e);
                    }
                })
                .flatMap(fetchResult -> { // FetchResult 타입 명시 제거
                    if (fetchResult.getData().isEmpty()) {
                        log.warn("API에서 가져온 데이터가 없습니다.");
                        return Mono.just(Collections.<DisabledJobseekerDTO>emptyList());
                    } else {
                        List<String> newDataIds = fetchResult.getData().stream()
                                .map(DisabledJobseekerDTO::getId)
                                .collect(Collectors.toList());
                        List<DisabledJobseekerDTO> existingDataInDB = repository.findByIdIn(newDataIds); // 변수명 변경
                        Set<String> existingIdsInDB = existingDataInDB.stream() // 변수명 변경
                                .map(DisabledJobseekerDTO::getId)
                                .collect(Collectors.toSet());
                        List<DisabledJobseekerDTO> newDataOnly = fetchResult.getData().stream()
                                .filter(dto -> !existingIdsInDB.contains(dto.getId()))
                                .collect(Collectors.toList());
                        
                        if (!newDataOnly.isEmpty()) {
                            repository.saveAll(newDataOnly);
                            log.info("{}개의 신규 데이터를 MongoDB에 저장했습니다", newDataOnly.size());
                        } else {
                            log.info("페이지 {}의 모든 데이터가 이미 DB에 존재합니다", page);
                        }
                        
                        boolean hasMorePages = hasMorePages(page, perPage, fetchResult.getTotalCount());
                        if (hasMorePages) {
                            log.info("다음 페이지가 있습니다. 페이지 {} 조회 중...", page + 1);
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
                    return Mono.just(Collections.<DisabledJobseekerDTO>emptyList());
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("API 처리 중 예외 발생: {}", e.getMessage(), e);
                    return Mono.just(Collections.<DisabledJobseekerDTO>emptyList());
                });
    }
    
    private boolean hasMorePages(int currentPage, int perPage, int totalCount) {
        if (perPage <= 0) return false; // perPage가 0 이하인 경우 방지
        int maxPages = (int) Math.ceil((double) totalCount / perPage);
        return currentPage < maxPages;
    }
    
    private static class FetchResult {
        private final List<DisabledJobseekerDTO> data;
        private final int totalCount;
        public FetchResult(List<DisabledJobseekerDTO> data, int totalCount) {
            this.data = data;
            this.totalCount = totalCount;
        }
        public List<DisabledJobseekerDTO> getData() { return data; }
        public int getTotalCount() { return totalCount; }
    }
    
    private void createCollectionIfNotExists() {
        if (!mongoTemplate.collectionExists(DisabledJobseekerDTO.class)) {
            mongoTemplate.createCollection(DisabledJobseekerDTO.class);
            log.info("'disabled_jobseekers' 컬렉션 생성 완료");
        } else {
            log.info("'disabled_jobseekers' 컬렉션 확인 완료 (이미 존재)");
        }
    }

    public List<DisabledJobseekerDTO> findAll() {
        return repository.findAll();
    }
    
    public Map<String, Object> findPaged(int page, int size) {
        return findPaged(page, size, null, null);
    }
    
    public Map<String, Object> findPaged(int page, int size, String search, List<String> disabilityTypes) {
        log.info("페이징 처리된 장애인 구직자 데이터 조회 - 페이지: {}, 크기: {}, 검색어: {}, 장애유형: {}", 
                 page, size, search, disabilityTypes);
        int pageIndex = Math.max(0, page - 1);
        
        try {
            List<DisabledJobseekerDTO> pagedData;
            long totalItems; // long 타입으로 변경
            
            if ((search == null || search.trim().isEmpty()) && (disabilityTypes == null || disabilityTypes.isEmpty())) {
                Pageable pageable = PageRequest.of(pageIndex, size);
                Page<DisabledJobseekerDTO> dataPage = repository.findAll(pageable);
                pagedData = dataPage.getContent();
                totalItems = dataPage.getTotalElements();
                log.info("MongoDB 페이징을 사용하여 데이터 조회 완료 - 결과 수: {}, 총 항목 수: {}", pagedData.size(), totalItems);
            } else {
                log.info("필터링이 필요한 조회 수행");
                List<DisabledJobseekerDTO> filteredData;
                if (search != null && !search.trim().isEmpty()) {
                    String searchLower = search.toLowerCase(); // 이미 Repository에서 i 옵션 사용 중
                    if (disabilityTypes != null && !disabilityTypes.isEmpty()) {
                        filteredData = repository.findBySearchTextAndDisabilityTypes(search, disabilityTypes); // 원본 search 사용
                        log.info("검색어와 장애유형으로 필터링된 데이터 수: {}", filteredData.size());
                    } else {
                        filteredData = repository.findBySearchText(search); // 원본 search 사용
                        log.info("검색어로 필터링된 데이터 수: {}", filteredData.size());
                    }
                } else if (disabilityTypes != null && !disabilityTypes.isEmpty()) {
                    filteredData = repository.findByDisabilityTypes(disabilityTypes);
                    log.info("장애유형으로 필터링된 데이터 수: {}", filteredData.size());
                } else {
                    filteredData = Collections.emptyList();
                }
                totalItems = filteredData.size();
                int fromIndex = pageIndex * size;
                int toIndex = Math.min(fromIndex + size, (int) totalItems); // totalItems를 int로 캐스팅
                pagedData = (fromIndex < toIndex && fromIndex < totalItems) ? filteredData.subList(fromIndex, toIndex) : Collections.emptyList(); // fromIndex < totalItems 조건 추가
                log.info("메모리 내 페이징을 사용하여 필터링된 데이터 조회 완료 - 결과 수: {}", pagedData.size());
            }
            
            int totalPages = (size > 0) ? (int) Math.ceil((double) totalItems / size) : 0; // size가 0인 경우 방지
            
            Map<String, Object> result = new HashMap<>();
            result.put("content", pagedData);
            result.put("totalItems", totalItems);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            return result;
        } catch (Exception e) {
            log.error("페이징 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("content", Collections.emptyList());
            errorResult.put("totalItems", 0L); // long 타입
            errorResult.put("totalPages", 0);
            errorResult.put("currentPage", page);
            errorResult.put("error", "데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
            return errorResult;
        }
    }
    
    public Optional<DisabledJobseekerDTO> findById(String id) {
        return repository.findById(id);
    }
    
    public List<String> getDisabilityTypes() {
        try {
            List<String> types = mongoTemplate.getCollection("disabled_jobseekers")
                .distinct("장애유형", String.class)
                .filter(new org.bson.Document("장애유형", new org.bson.Document("$ne", null).append("$ne", ""))) // null 및 빈 문자열 제외
                .into(new ArrayList<>());
            Collections.sort(types); // 정렬
            return types;
        } catch (Exception e) {
            log.error("장애유형 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    public void clearAll() {
        repository.deleteAll();
        log.info("disabled_jobseekers 컬렉션의 모든 데이터가 삭제되었습니다.");
    }
    
    public String getServiceStatus() {
        return "정상 작동 중";
    }

    private int parseIntSafely(Object value) {
        if (value == null) return 0;
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            String strValue = String.valueOf(value).trim();
            if (strValue.isEmpty()) return 0;
            return Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            log.warn("정수 파싱 오류 값: '{}'", value, e);
            return 0;
        }
    }

    public DisabilityTypeStatsDTO getDisabilityTypeStats(String disabilityType) {
        log.info("장애유형별 통계 계산 시작 - 장애유형: {}", disabilityType);
        if (disabilityType == null || disabilityType.trim().isEmpty()) {
            throw new IllegalArgumentException("장애유형은 필수 파라미터입니다.");
        }
        
        DisabilityTypeStatsDTO stats = new DisabilityTypeStatsDTO();
        stats.setDisabilityType(disabilityType);
        
        try {
            long totalCount = repository.count();
            stats.setTotalCount(totalCount);
            
            Criteria disabilityCriteria = Criteria.where("장애유형").is(disabilityType);
            long disabilityTypeCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(disabilityCriteria), DisabledJobseekerDTO.class);
            stats.setDisabilityTypeCount(disabilityTypeCount);
            
            if (totalCount > 0) { // 0으로 나누는 것 방지
                double percentage = (double) disabilityTypeCount / totalCount * 100;
                stats.setPercentage(Math.round(percentage * 100) / 100.0);
            } else {
                stats.setPercentage(0.0);
            }
            
            stats.setSeverityDistribution(getSeverityDistribution(disabilityType));
            stats.setRegionDistribution(getRegionDistribution(disabilityType));
            stats.setAgeDistribution(getAgeDistribution(disabilityType));
            stats.setJobTypeDistribution(getJobTypeDistribution(disabilityType));
            stats.setSalaryDistribution(getSalaryDistribution(disabilityType));
            stats.setCombinedStats(getCombinedStats(disabilityType, disabilityTypeCount)); // totalCount 대신 disabilityTypeCount 사용
            
            log.info("장애유형별 통계 계산 완료 - 장애유형: {}, 인원: {}/{} ({}%)",
                    disabilityType, disabilityTypeCount, totalCount, stats.getPercentage());
            return stats;
        } catch (Exception e) {
            log.error("장애유형별 통계 계산 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시, 기본값으로 채워진 stats 객체 반환 고려 또는 예외 전파
            // 여기서는 예외를 다시 던져 상위에서 처리하도록 함
            throw new RuntimeException("통계 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private Map<String, Long> getSeverityDistribution(String disabilityType) {
        try {
            MatchOperation matchOperation = Aggregation.match(Criteria.where("장애유형").is(disabilityType));
            GroupOperation groupOperation = Aggregation.group("중증여부").count().as("count");
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);
            
            AggregationResults<DistributionItemDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", DistributionItemDTO.class);
            
            Map<String, Long> distribution = new HashMap<>();
            for (DistributionItemDTO item : results.getMappedResults()) {
                String key = item.get_id() != null ? item.get_id().toString() : "미분류";
                distribution.put(key, item.getCount());
            }
            log.debug("[getSeverityDistribution] for {}: {}", disabilityType, distribution);
            return distribution;
        } catch (Exception e) {
            log.error("중증/경증 분포 집계 중 오류 ({}): {}", disabilityType, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
    
    private Map<String, Long> getRegionDistribution(String disabilityType) {
        try {
            MatchOperation matchOperation = Aggregation.match(
                Criteria.where("장애유형").is(disabilityType)
                        .and("희망지역").exists(true).ne(null).ne("")
            );
            
            ProjectionOperation projectOperation = Aggregation.project()
                    .and(ctx -> Document.parse("{ $arrayElemAt: [{ $split: ['$희망지역', ' '] }, 0] }")).as("시도지역");

            GroupOperation groupOperation = Aggregation.group("시도지역").count().as("count");
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectOperation, groupOperation);
            
            AggregationResults<DistributionItemDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", DistributionItemDTO.class);
            
            Map<String, Long> distribution = new HashMap<>();
            for (DistributionItemDTO item : results.getMappedResults()) {
                String key = item.get_id() != null ? item.get_id().toString() : "미분류";
                distribution.put(key, item.getCount());
            }
            log.debug("[getRegionDistribution] for {}: {}", disabilityType, distribution); 
            return distribution;
        } catch (Exception e) {
            log.error("지역별 분포 집계 중 오류 ({}): {}", disabilityType, e.getMessage(), e); 
            return Collections.emptyMap();
        }
    }
    
    private Map<String, Long> getAgeDistribution(String disabilityType) {
        try {
            MatchOperation matchOperation = Aggregation.match(
                Criteria.where("장애유형").is(disabilityType)
                        .and("연령").exists(true).ne(null)
            );

            String ageProjectionExpression = "{ $cond: { " +
                                             "  if: { $and: [ { $ne: ['$연령', null] }, { $in: [ { $type: '$연령' }, ['int', 'long', 'double', 'decimal'] ] } ] }, " +
                                             "  then: { $concat: [{ $toString: { $floor: { $divide: ['$연령', 10] } } }, '0대'] }, " +
                                             "  else: '미분류' " +
                                             "}}";
            ProjectionOperation projectOperation = Aggregation.project()
                    .and(ctx -> Document.parse(ageProjectionExpression)).as("연령대");

            GroupOperation groupOperation = Aggregation.group("연령대").count().as("count");
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectOperation, groupOperation);
            
            AggregationResults<DistributionItemDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", DistributionItemDTO.class);
            
            Map<String, Long> distribution = new HashMap<>();
            for (DistributionItemDTO item : results.getMappedResults()) {
                String key = item.get_id() != null ? item.get_id().toString() : "미분류";
                distribution.put(key, item.getCount());
            }
            log.debug("[getAgeDistribution] for {}: {}", disabilityType, distribution); 
            return distribution;
        } catch (Exception e) {
            log.error("연령대별 분포 집계 중 오류 ({}): {}", disabilityType, e.getMessage(), e); 
            return Collections.emptyMap();
        }
    }
    
    private Map<String, Long> getJobTypeDistribution(String disabilityType) {
        try {
            MatchOperation matchOperation = Aggregation.match(Criteria.where("장애유형").is(disabilityType));
            GroupOperation groupOperation = Aggregation.group("희망직종").count().as("count");
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);
            
            AggregationResults<DistributionItemDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", DistributionItemDTO.class);
            
            Map<String, Long> distribution = new HashMap<>();
            for (DistributionItemDTO item : results.getMappedResults()) {
                String key = item.get_id() != null ? item.get_id().toString() : "미분류";
                distribution.put(key, item.getCount());
            }
            log.debug("[getJobTypeDistribution] for {}: {}", disabilityType, distribution);
            return distribution;
        } catch (Exception e) {
            log.error("희망직종별 분포 집계 중 오류 ({}): {}", disabilityType, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
    
    private Map<String, Long> getSalaryDistribution(String disabilityType) {
        try {
            MatchOperation matchOperation = Aggregation.match(Criteria.where("장애유형").is(disabilityType));
            GroupOperation groupOperation = Aggregation.group("희망임금").count().as("count");
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);
            
            AggregationResults<DistributionItemDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", DistributionItemDTO.class);
            
            Map<String, Long> distribution = new HashMap<>();
            for (DistributionItemDTO item : results.getMappedResults()) {
                String key = item.get_id() != null ? item.get_id().toString() : "미분류";
                distribution.put(key, item.getCount());
            }
            log.debug("[getSalaryDistribution] for {}: {}", disabilityType, distribution);
            return distribution;
        } catch (Exception e) {
            log.error("희망임금별 분포 집계 중 오류 ({}): {}", disabilityType, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
    
    private List<DisabilityTypeStatsDTO.CombinedStatItem> getCombinedStats(String disabilityType, long disabilityTypeTotalCount) {
        try {
            MatchOperation matchOperation = Aggregation.match(
                Criteria.where("장애유형").is(disabilityType)
                        .and("희망지역").exists(true).ne(null).ne("")
                        .and("연령").exists(true).ne(null)
            );
            
            String ageGroupProjectionExpression = "{ $cond: { " +
                                                 "  if: { $and: [ { $ne: ['$연령', null] }, { $in: [ { $type: '$연령' }, ['int', 'long', 'double', 'decimal'] ] } ] }, " +
                                                 "  then: { $concat: [{ $toString: { $floor: { $divide: ['$연령', 10] } } }, '0대'] }, " +
                                                 "  else: '미분류' " +
                                                 "}}";

            ProjectionOperation projectOperation = Aggregation.project()
                    .and("중증여부").as("severity")
                    .and(ctx -> Document.parse("{ $arrayElemAt: [{ $split: ['$희망지역', ' '] }, 0] }")).as("region")
                    .and(ctx -> Document.parse(ageGroupProjectionExpression)).as("ageGroup") 
                    .and("희망직종").as("jobType")
                    .and("희망임금").as("salary");
            
            GroupOperation groupOperation = Aggregation.group("severity", "region", "ageGroup", "jobType", "salary")
                    .count().as("count");
            
            org.springframework.data.domain.Sort.Direction sortDirection = org.springframework.data.domain.Sort.Direction.DESC;
            org.springframework.data.mongodb.core.aggregation.SortOperation sortOperation = 
                    Aggregation.sort(org.springframework.data.domain.Sort.by(sortDirection, "count"));
            org.springframework.data.mongodb.core.aggregation.LimitOperation limitOperation = Aggregation.limit(10);
            
            Aggregation aggregation = Aggregation.newAggregation(
                    matchOperation, projectOperation, groupOperation, sortOperation, limitOperation);
            
            AggregationResults<CombinedAggregationResultDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", CombinedAggregationResultDTO.class);
            
            List<DisabilityTypeStatsDTO.CombinedStatItem> combinedStats = new ArrayList<>();
            for (CombinedAggregationResultDTO aggResult : results.getMappedResults()) {
                CombinedStatsIdDTO idDoc = aggResult.get_id();
                if (idDoc == null) continue;

                DisabilityTypeStatsDTO.CombinedStatItem item = new DisabilityTypeStatsDTO.CombinedStatItem();
                item.setSeverity(idDoc.getSeverity() != null ? idDoc.getSeverity() : "미분류");
                item.setRegion(idDoc.getRegion() != null ? idDoc.getRegion() : "미분류");
                item.setAgeGroup(idDoc.getAgeGroup() != null ? idDoc.getAgeGroup() : "미분류");
                item.setJobType(idDoc.getJobType() != null ? idDoc.getJobType() : "미분류");
                item.setSalary(idDoc.getSalary() != null ? idDoc.getSalary() : "미분류");
                
                long count = aggResult.getCount();
                item.setCount(count);
                
                if (disabilityTypeTotalCount > 0) { 
                    double percent = (double) count / disabilityTypeTotalCount * 100;
                    item.setPercentOfTotal(Math.round(percent * 100) / 100.0);
                } else {
                    item.setPercentOfTotal(0.0);
                }
                combinedStats.add(item);
            }
            log.debug("[getCombinedStats] for {}: {} items", disabilityType, combinedStats.size());
            return combinedStats;
        } catch (Exception e) {
            log.error("복합 통계 집계 중 오류 ({}): {}", disabilityType, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private String mongoExpression(String expression) { // 이 메소드는 현재 사용되지 않음
        return expression;
    }

    public RegionStatsDTO getRegionStats(String region) {
        log.info("지역별 통계 계산 시작 - 지역: {}", region);
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("지역은 필수 파라미터입니다.");
        }
        
        RegionStatsDTO stats = new RegionStatsDTO();
        stats.setRegion(region);
        
        try {
            // 전체 구직자 수 조회
            long totalCount = repository.count();
            stats.setTotalCount(totalCount);
            
            // 지역별 구직자 수 조회
            Criteria regionCriteria = Criteria.where("희망지역").regex("^" + region);
            long regionCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(regionCriteria), DisabledJobseekerDTO.class);
            
            // 장애유형별 분포
            stats.setDisabilityTypeDistribution(getRegionDisabilityTypeDistribution(region));
            
            // 연령대별 분포
            stats.setAgeDistribution(getRegionAgeDistribution(region));
            
            // 희망직종별 분포
            stats.setJobTypeDistribution(getRegionJobTypeDistribution(region));
            
            // 희망임금별 분포
            stats.setSalaryDistribution(getRegionSalaryDistribution(region));
            
            // 중증/경증 분포
            stats.setSeverityDistribution(getRegionSeverityDistribution(region));
            
            log.info("지역별 통계 계산 완료 - 지역: {}, 인원: {}", region, regionCount);
            return stats;
        } catch (Exception e) {
            log.error("지역별 통계 계산 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("통계 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private Map<String, Long> getRegionDisabilityTypeDistribution(String region) {
        try {
            MatchOperation matchOperation = Aggregation.match(
                Criteria.where("희망지역").regex("^" + region)
            );
            GroupOperation groupOperation = Aggregation.group("장애유형").count().as("count");
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);
            
            AggregationResults<DistributionItemDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", DistributionItemDTO.class);
            
            Map<String, Long> distribution = new HashMap<>();
            for (DistributionItemDTO item : results.getMappedResults()) {
                String key = item.get_id() != null ? item.get_id().toString() : "미분류";
                distribution.put(key, item.getCount());
            }
            return distribution;
        } catch (Exception e) {
            log.error("지역별 장애유형 분포 집계 중 오류 ({}): {}", region, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private Map<String, Long> getRegionAgeDistribution(String region) {
        try {
            MatchOperation matchOperation = Aggregation.match(
                Criteria.where("희망지역").regex("^" + region)
                        .and("연령").exists(true).ne(null)
            );

            String ageProjectionExpression = "{ $cond: { " +
                                             "  if: { $and: [ { $ne: ['$연령', null] }, { $in: [ { $type: '$연령' }, ['int', 'long', 'double', 'decimal'] ] } ] }, " +
                                             "  then: { $concat: [{ $toString: { $floor: { $divide: ['$연령', 10] } } }, '0대'] }, " +
                                             "  else: '미분류' " +
                                             "}}";
            ProjectionOperation projectOperation = Aggregation.project()
                    .and(ctx -> Document.parse(ageProjectionExpression)).as("연령대");

            GroupOperation groupOperation = Aggregation.group("연령대").count().as("count");
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectOperation, groupOperation);
            
            AggregationResults<DistributionItemDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", DistributionItemDTO.class);
            
            Map<String, Long> distribution = new HashMap<>();
            for (DistributionItemDTO item : results.getMappedResults()) {
                String key = item.get_id() != null ? item.get_id().toString() : "미분류";
                distribution.put(key, item.getCount());
            }
            return distribution;
        } catch (Exception e) {
            log.error("지역별 연령대 분포 집계 중 오류 ({}): {}", region, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private Map<String, Long> getRegionJobTypeDistribution(String region) {
        try {
            MatchOperation matchOperation = Aggregation.match(
                Criteria.where("희망지역").regex("^" + region)
            );
            GroupOperation groupOperation = Aggregation.group("희망직종").count().as("count");
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);
            
            AggregationResults<DistributionItemDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", DistributionItemDTO.class);
            
            Map<String, Long> distribution = new HashMap<>();
            for (DistributionItemDTO item : results.getMappedResults()) {
                String key = item.get_id() != null ? item.get_id().toString() : "미분류";
                distribution.put(key, item.getCount());
            }
            return distribution;
        } catch (Exception e) {
            log.error("지역별 희망직종 분포 집계 중 오류 ({}): {}", region, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private Map<String, Long> getRegionSalaryDistribution(String region) {
        try {
            MatchOperation matchOperation = Aggregation.match(
                Criteria.where("희망지역").regex("^" + region)
            );
            GroupOperation groupOperation = Aggregation.group("희망임금").count().as("count");
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);
            
            AggregationResults<DistributionItemDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", DistributionItemDTO.class);
            
            Map<String, Long> distribution = new HashMap<>();
            for (DistributionItemDTO item : results.getMappedResults()) {
                String key = item.get_id() != null ? item.get_id().toString() : "미분류";
                distribution.put(key, item.getCount());
            }
            return distribution;
        } catch (Exception e) {
            log.error("지역별 희망임금 분포 집계 중 오류 ({}): {}", region, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private Map<String, Long> getRegionSeverityDistribution(String region) {
        try {
            MatchOperation matchOperation = Aggregation.match(
                Criteria.where("희망지역").regex("^" + region)
            );
            GroupOperation groupOperation = Aggregation.group("중증여부").count().as("count");
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation);
            
            AggregationResults<DistributionItemDTO> results = mongoTemplate.aggregate(
                    aggregation, "disabled_jobseekers", DistributionItemDTO.class);
            
            Map<String, Long> distribution = new HashMap<>();
            for (DistributionItemDTO item : results.getMappedResults()) {
                String key = item.get_id() != null ? item.get_id().toString() : "미분류";
                distribution.put(key, item.getCount());
            }
            return distribution;
        } catch (Exception e) {
            log.error("지역별 중증/경증 분포 집계 중 오류 ({}): {}", region, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}
