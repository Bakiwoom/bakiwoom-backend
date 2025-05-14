package com.javaex.idea.service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.javaex.config.ApiKeyConfig;
import com.javaex.idea.dto.DisabledJobofferDTO;
import com.javaex.idea.repository.DisabledJobofferRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisabledJobofferService {
    
    private final WebClient webClient;
    private final ApiKeyConfig apiKeyConfig;
    private final DisabledJobofferRepository repository;
    private final MongoTemplate mongoTemplate;
    
    private static final String API_URL = "https://apis.data.go.kr/B552583/job/job_list_env";
    private static final int MAX_PAGES = 10; // 최대 10페이지까지 조회 (1000개)
    
    public Mono<List<DisabledJobofferDTO>> fetchAndSaveData() {
        log.info("[fetchAndSaveData] 데이터 갱신 시작");
        return fetchDataFromAllPages()
                .flatMap(data -> {
                    log.info("[fetchAndSaveData] 저장할 데이터 개수: {}", data.size());
                    if (!data.isEmpty()) {
                        saveWithoutDuplicates(data);
                        log.info("[fetchAndSaveData] MongoDB 저장 완료");
                    } else {
                        log.warn("[fetchAndSaveData] 저장할 데이터가 없습니다.");
                    }
                    return Mono.just(data);
                });
    }
    
    // 중복 확인 후 신규 데이터만 저장
    private void saveWithoutDuplicates(List<DisabledJobofferDTO> newData) {
        // 새 데이터의 ID 목록 추출
        List<String> newIds = newData.stream()
                .map(DisabledJobofferDTO::getId)
                .collect(Collectors.toList());
        
        // 기존 데이터 중에서 새 데이터와 ID가 같은 것만 조회
        Query query = new Query(Criteria.where("id").in(newIds));
        List<DisabledJobofferDTO> existingData = mongoTemplate.find(query, DisabledJobofferDTO.class);
        
        // 기존 데이터의 ID 집합
        Set<String> existingIds = existingData.stream()
                .map(DisabledJobofferDTO::getId)
                .collect(Collectors.toSet());
        
        // 중복되지 않은 데이터만 필터링
        List<DisabledJobofferDTO> uniqueData = newData.stream()
                .filter(dto -> !existingIds.contains(dto.getId()))
                .collect(Collectors.toList());
        
        log.info("[saveWithoutDuplicates] 기존 데이터: {}, 새 데이터: {}, 중복 제외 저장: {}", 
                existingData.size(), newData.size(), uniqueData.size());
        
        if (!uniqueData.isEmpty()) {
            repository.saveAll(uniqueData);
        }
    }
    
    // 모든 페이지에서 데이터 가져오기
    private Mono<List<DisabledJobofferDTO>> fetchDataFromAllPages() {
        List<Mono<List<DisabledJobofferDTO>>> pageRequests = new ArrayList<>();
        
        // 여러 페이지 요청 생성
        for (int page = 1; page <= MAX_PAGES; page++) {
            pageRequests.add(fetchDataFromPage(page, 100));
        }
        
        // 모든 페이지 요청을 병합
        return Mono.zip(pageRequests, responses -> {
            List<DisabledJobofferDTO> allData = new ArrayList<>();
            for (Object response : responses) {
                @SuppressWarnings("unchecked")
                List<DisabledJobofferDTO> pageData = (List<DisabledJobofferDTO>) response;
                allData.addAll(pageData);
            }
            log.info("[fetchDataFromAllPages] 모든 페이지에서 가져온 데이터 수: {}", allData.size());
            return allData;
        });
    }
    
    // 특정 페이지에서 데이터 가져오기
    private Mono<List<DisabledJobofferDTO>> fetchDataFromPage(int pageNo, int numOfRows) {
        String url = API_URL
            + "?serviceKey=" + apiKeyConfig.getEncodedKey()
            + "&pageNo=" + pageNo
            + "&numOfRows=" + numOfRows;
        log.info("[fetchDataFromPage] 페이지 {} 요청 URL: {}", pageNo, url);
        
        return webClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String.class)
                .map(xml -> parseXmlResponse(xml, pageNo))
                .onErrorResume(e -> {
                    log.error("[fetchDataFromPage] 페이지 {} 요청 오류: {}", pageNo, e.getMessage());
                    return Mono.just(new ArrayList<>());
                });
    }
    
    private List<DisabledJobofferDTO> parseXmlResponse(String xmlResponse, int pageNo) {
        List<DisabledJobofferDTO> joboffers = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));
            log.info("[parseXmlResponse] 페이지 {} XML 파싱 시작", pageNo);
            NodeList items = document.getElementsByTagName("item");
            log.info("[parseXmlResponse] 페이지 {} item 태그 개수: {}", pageNo, items.getLength());
            
            // 더 이상 데이터가 없으면 빈 리스트 반환
            if (items.getLength() == 0) {
                log.info("[parseXmlResponse] 페이지 {}에 더 이상 데이터가 없습니다", pageNo);
                return joboffers;
            }
            
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                DisabledJobofferDTO dto = new DisabledJobofferDTO();
                dto.setBusplaName(getElementValue(item, "busplaName"));
                dto.setCntctNo(getElementValue(item, "cntctNo"));
                dto.setCompAddr(getElementValue(item, "compAddr"));
                dto.setEmpType(getElementValue(item, "empType"));
                dto.setEnterType(getElementValue(item, "enterType"));
                dto.setJobNm(getElementValue(item, "jobNm"));
                dto.setOfferregDt(getElementValue(item, "offerregDt"));
                dto.setRegDt(getElementValue(item, "regDt"));
                dto.setRegagnName(getElementValue(item, "regagnName"));
                dto.setReqCareer(getElementValue(item, "reqCareer"));
                dto.setReqEduc(getElementValue(item, "reqEduc"));
                dto.setRno(getElementValue(item, "rno"));
                dto.setRnum(getElementValue(item, "rnum"));
                dto.setSalary(getElementValue(item, "salary"));
                dto.setSalaryType(getElementValue(item, "salaryType"));
                dto.setTermDate(getElementValue(item, "termDate"));
                dto.setEnvBothHands(getElementValue(item, "envBothHands"));
                dto.setEnvEyesight(getElementValue(item, "envEyesight"));
                dto.setEnvHandwork(getElementValue(item, "envHandwork"));
                dto.setEnvLiftPower(getElementValue(item, "envLiftPower"));
                dto.setEnvLstnTalk(getElementValue(item, "envLstnTalk"));
                dto.setEnvStndWalk(getElementValue(item, "envStndWalk"));
                dto.setId(dto.getRno() + "_" + dto.getRnum());
                joboffers.add(dto);
            }
            log.info("[parseXmlResponse] 페이지 {} 파싱된 데이터 개수: {}", pageNo, joboffers.size());
        } catch (Exception e) {
            log.error("[parseXmlResponse] 페이지 {} XML 파싱 오류", pageNo, e);
        }
        return joboffers;
    }
    
    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
    
    public List<DisabledJobofferDTO> findByFilters(String region, String disabilityType, String age) {
        log.info("[findByFilters] 필터링 시작 - region: {}, disabilityType: {}, age: {}", region, disabilityType, age);
        
        Query query = new Query();
        
        if (region != null) {
            query.addCriteria(Criteria.where("region").is(region));
        }
        if (disabilityType != null) {
            query.addCriteria(Criteria.where("disabilityType").is(disabilityType));
        }
        if (age != null) {
            query.addCriteria(Criteria.where("age").is(age));
        }
        
        List<DisabledJobofferDTO> results = mongoTemplate.find(query, DisabledJobofferDTO.class);
        log.info("[findByFilters] 조회된 데이터 개수: {}", results.size());
        return results;
    }
} 