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
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.javaex.idea.repository.DisabledJobseekerRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisabledJobseekerService {

    private final WebClient webClient;
    private final ApiKeyConfig apiKeyConfig;
    private final ObjectMapper objectMapper;
    private final DisabledJobseekerRepository repository;

    // 공식 문서에 제공된 API URL 정보
    private static final String API_URL = "https://api.odcloud.kr/api/15014774/v1/uddi:bed031bf-2d7b-40ee-abef-b8e8ea0b0467";
    private static final String JSON_FILE_URL = "https://www.data.go.kr/catalog/15014774/fileData.json";
    private static final String DIRECT_DOWNLOAD_URL = "https://www.data.go.kr/upload/data/15014774/fileData.json";
    private static final int MAX_RETRY_PAGES = 5; // 최대 재시도 페이지 수

    // 데이터를 가져와 저장
    public Mono<List<DisabledJobseekerDTO>> fetchAndSaveData(int page, int perPage) {
        // 몽고DB 컬렉션이 존재하는지 확인 및 생성
        createCollectionIfNotExists();
        return fetchDataWithoutDuplicates(page, perPage, new HashSet<>(), 0);
    }
    
    // 중복 없이 데이터 가져오기 (재귀적으로 구현)
    private Mono<List<DisabledJobseekerDTO>> fetchDataWithoutDuplicates(int page, int perPage, Set<Integer> processedIds, int retryCount) {
        if (retryCount >= MAX_RETRY_PAGES) {
            log.warn("최대 재시도 페이지 수({})에 도달했습니다. 더 이상 새 데이터를 가져오지 않습니다.", MAX_RETRY_PAGES);
            return Mono.just(Collections.<DisabledJobseekerDTO>emptyList());
        }
        
        log.info("페이지 {}: 데이터 가져오기 시도 중", page);
        
        String url = API_URL
            + "?serviceKey=" + apiKeyConfig.getEncodedKey()
            + "&page=" + page
            + "&perPage=" + perPage
            ;
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
                        
                        log.info("API에서 가져온 데이터 수: {}", dataItems.size());
                        
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
                        return dtoList;
                    } catch (Exception e) {
                        log.error("API 응답 처리 중 오류: {}", e.getMessage(), e);
                        throw new RuntimeException("API 응답 데이터 처리 중 오류 발생", e);
                    }
                })
                .flatMap(apiList -> {
                    if (apiList.isEmpty()) {
                        log.warn("API에서 가져온 데이터가 없습니다. JSON 파일 다운로드를 시도합니다.");
                        // JSON 파일 다운로드 시도
                        return tryJsonFileDownload(page, perPage);
                    } else {
                        // 기존 데이터와 비교하여 중복 여부 확인
                        List<String> newDataIds = apiList.stream()
                                .map(DisabledJobseekerDTO::getId)
                                .collect(Collectors.toList());
                        
                        // 기존 DB에서 이 연번들이 있는지 확인
                        List<DisabledJobseekerDTO> existingData = repository.findByIdIn(newDataIds);
                        
                        // 기존 데이터의 연번 집합
                        Set<Integer> existingIds = existingData.stream()
                                .map(DisabledJobseekerDTO::get연번)
                                .collect(Collectors.toSet());
                        
                        // 처리된 ID 집합에 기존 ID 추가
                        processedIds.addAll(existingIds);
                        
                        // 신규 데이터만 필터링
                        List<DisabledJobseekerDTO> newDataOnly = apiList.stream()
                                .filter(dto -> !existingIds.contains(dto.get연번()))
                                .collect(Collectors.toList());
                        
                        if (newDataOnly.isEmpty()) {
                            log.info("페이지 {}의 모든 데이터가 이미 DB에 존재합니다. 다음 페이지 시도...", page);
                            // 재귀적으로 다음 페이지 호출
                            return fetchDataWithoutDuplicates(page + 1, perPage, processedIds, retryCount + 1);
                        } else {
                            // 신규 데이터만 저장
                            repository.saveAll(newDataOnly);
                            log.info("{}개의 신규 데이터를 MongoDB에 저장했습니다", newDataOnly.size());
                            
                            // 페이지 내 데이터가 모두 중복이 아닌 경우, 다음 페이지도 가져옴
                            if (newDataOnly.size() < apiList.size()) {
                                log.info("페이지 {}에서 일부 데이터만 신규입니다. 다음 페이지 시도...", page);
                                // 재귀적으로 다음 페이지 호출하고 결과 합침
                                return fetchDataWithoutDuplicates(page + 1, perPage, processedIds, 0)
                                        .map(nextPageData -> {
                                            List<DisabledJobseekerDTO> combinedList = new ArrayList<>(newDataOnly);
                                            combinedList.addAll(nextPageData);
                                            return combinedList;
                                        });
                            }
                            
                            return Mono.just(newDataOnly);
                        }
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("API 요청 오류: {}, 상태 코드: {}, 응답 본문: {}", 
                            e.getMessage(), e.getStatusCode(), e.getResponseBodyAsString());
                    // API 호출 실패 시 JSON 파일 다운로드 시도
                    return tryJsonFileDownload(page, perPage);
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("API 처리 중 예외 발생: {}", e.getMessage(), e);
                    // 예외 발생 시 JSON 파일 다운로드 시도
                    return tryJsonFileDownload(page, perPage);
                });
    }
    
    // JSON 파일 다운로드 시도
    private Mono<List<DisabledJobseekerDTO>> tryJsonFileDownload(int page, int perPage) {
        log.info("JSON 파일 다운로드 시도: {}", JSON_FILE_URL);
        
        return webClient.get()
                .uri(JSON_FILE_URL)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(jsonContent -> {
                    try {
                        log.info("JSON 파일 다운로드 성공, 데이터 처리 중...");
                        List<DisabledJobseekerDTO> parsedData = parseJsonFile(jsonContent, page, perPage);
                        if (!parsedData.isEmpty()) {
                            // 기존 데이터와 비교하여 중복 필터링
                            List<String> newDataIds = parsedData.stream()
                                    .map(DisabledJobseekerDTO::getId)
                                    .collect(Collectors.toList());
                            
                            List<DisabledJobseekerDTO> existingData = repository.findByIdIn(newDataIds);
                            
                            Set<Integer> existingIds = existingData.stream()
                                    .map(DisabledJobseekerDTO::get연번)
                                    .collect(Collectors.toSet());
                            
                            List<DisabledJobseekerDTO> newDataOnly = parsedData.stream()
                                    .filter(dto -> !existingIds.contains(dto.get연번()))
                                    .collect(Collectors.toList());
                            
                            if (!newDataOnly.isEmpty()) {
                                repository.saveAll(newDataOnly);
                                log.info("{}개의 신규 JSON 파일 데이터를 MongoDB에 저장했습니다", newDataOnly.size());
                                return Mono.just(newDataOnly);
                            } else {
                                log.info("JSON 파일에서 가져온 모든 데이터가 이미 DB에 존재합니다.");
                                return Mono.just(Collections.<DisabledJobseekerDTO>emptyList());
                            }
                        } else {
                            log.warn("JSON 파일에서 추출한 데이터가 없습니다. 직접 다운로드를 시도합니다.");
                            return tryDirectDownload(page, perPage);
                        }
                    } catch (Exception e) {
                        log.error("JSON 파일 처리 중 오류: {}", e.getMessage(), e);
                        return tryDirectDownload(page, perPage);
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("JSON 파일 다운로드 오류: {}, 상태 코드: {}", 
                            e.getMessage(), e.getStatusCode());
                    return tryDirectDownload(page, perPage);
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("JSON 파일 다운로드 중 예외 발생: {}", e.getMessage(), e);
                    return tryDirectDownload(page, perPage);
                });
    }
    
    // 직접 다운로드 URL을 통한 시도
    private Mono<List<DisabledJobseekerDTO>> tryDirectDownload(int page, int perPage) {
        log.info("직접 다운로드 URL 시도: {}", DIRECT_DOWNLOAD_URL);
        
        return Mono.fromCallable(() -> {
            try {
                // 임시 파일로 다운로드
                Path tempFile = downloadFile(DIRECT_DOWNLOAD_URL);
                if (tempFile != null) {
                    String content = new String(Files.readAllBytes(tempFile), StandardCharsets.UTF_8);
                    Files.deleteIfExists(tempFile); // 임시 파일 삭제
                    
                    List<DisabledJobseekerDTO> parsedData = parseJsonFile(content, page, perPage);
                    if (!parsedData.isEmpty()) {
                        // 기존 데이터와 비교하여 중복 필터링
                        List<String> newDataIds = parsedData.stream()
                                .map(DisabledJobseekerDTO::getId)
                                .collect(Collectors.toList());
                        
                        List<DisabledJobseekerDTO> existingData = repository.findByIdIn(newDataIds);
                        
                        Set<Integer> existingIds = existingData.stream()
                                .map(DisabledJobseekerDTO::get연번)
                                .collect(Collectors.toSet());
                        
                        List<DisabledJobseekerDTO> newDataOnly = parsedData.stream()
                                .filter(dto -> !existingIds.contains(dto.get연번()))
                                .collect(Collectors.toList());
                        
                        if (!newDataOnly.isEmpty()) {
                            repository.saveAll(newDataOnly);
                            log.info("{}개의 신규 직접 다운로드 데이터를 MongoDB에 저장했습니다", newDataOnly.size());
                            return newDataOnly;
                        } else {
                            log.info("직접 다운로드에서 가져온 모든 데이터가 이미 DB에 존재합니다.");
                            return Collections.<DisabledJobseekerDTO>emptyList();
                        }
                    }
                }
                log.warn("직접 다운로드 실패 또는 데이터 추출 실패");
                return Collections.<DisabledJobseekerDTO>emptyList();
            } catch (Exception e) {
                log.error("직접 다운로드 처리 중 오류: {}", e.getMessage(), e);
                return Collections.<DisabledJobseekerDTO>emptyList();
            }
        }).onErrorResume(e -> {
            log.error("직접 다운로드 중 예외 발생: {}", e.getMessage(), e);
            return Mono.just(Collections.<DisabledJobseekerDTO>emptyList());
        });
    }
    
    // 파일 다운로드 메서드
    private Path downloadFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                log.error("다운로드 실패: HTTP 오류 코드 {}", connection.getResponseCode());
                return null;
            }
            
            // 임시 파일 생성
            Path tempFile = Files.createTempFile("download-", ".json");
            
            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                log.info("파일 다운로드 성공: {}", tempFile);
                return tempFile;
            }
        } catch (IOException e) {
            log.error("파일 다운로드 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    // JSON 파일 데이터 파싱
    private List<DisabledJobseekerDTO> parseJsonFile(String jsonContent, int page, int perPage) {
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, Map.class);
            log.debug("JSON 파일 구조: {}", jsonMap.keySet());
            
            // 데이터 추출 위치 확인 (파일 구조에 따라 조정 필요)
            List<Map<String, Object>> dataItems = null;
            
            if (jsonMap.containsKey("data")) {
                dataItems = (List<Map<String, Object>>) jsonMap.get("data");
            } else if (jsonMap.containsKey("name") && jsonMap.containsKey("description")) {
                // 파일이 메타데이터인 경우, 내용을 분석하여 실제 데이터를 찾아야 함
                // 예시: 메타데이터에서 실제 데이터 위치를 파악하고 별도 요청할 수 있음
                log.info("다운로드한 파일은 메타데이터입니다. 추가 처리가 필요합니다.");
                return Collections.emptyList();
            }
            
            if (dataItems == null || dataItems.isEmpty()) {
                log.warn("JSON 파일에서 데이터를 찾을 수 없습니다.");
                return Collections.emptyList();
            }
            
            // 페이징 처리 (요청된 페이지와 개수에 맞게 데이터 추출)
            int startIndex = (page - 1) * perPage;
            int endIndex = Math.min(startIndex + perPage, dataItems.size());
            
            if (startIndex >= dataItems.size()) {
                log.warn("요청한 페이지가 데이터 범위를 벗어났습니다.");
                return Collections.emptyList();
            }
            
            List<DisabledJobseekerDTO> dtoList = new ArrayList<>();
            for (int i = startIndex; i < endIndex; i++) {
                try {
                    Map<String, Object> item = dataItems.get(i);
                    DisabledJobseekerDTO dto = new DisabledJobseekerDTO();
                    
                    // 필드 매핑 (JSON 파일 필드 -> DTO 필드)
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
                    log.error("JSON 항목 변환 중 오류: {}", e.getMessage(), e);
                }
            }
            
            log.info("JSON 파일에서 {}개의 데이터를 추출했습니다.", dtoList.size());
            return dtoList;
            
        } catch (Exception e) {
            log.error("JSON 파일 파싱 중 오류: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
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
    
    // ID로 특정 구직자 조회
    public Optional<DisabledJobseekerDTO> findById(String id) {
        return repository.findById(id);
    }
    
    // 데이터베이스 초기화 (테스트용)
    public void clearAll() {
        repository.deleteAll();
        log.info("disabled_jobseekers 컬렉션의 모든 데이터가 삭제되었습니다.");
    }
}
