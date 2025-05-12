package com.javaex.idea.service;



import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
    
    public Mono<List<DisabledJobofferDTO>> fetchAndSaveData() {
        log.info("[fetchAndSaveData] 데이터 갱신 시작");
        return fetchData()
                .flatMap(data -> {
                    log.info("[fetchAndSaveData] 저장할 데이터 개수: {}", data.size());
                    if (!data.isEmpty()) {
                        repository.saveAll(data);
                        log.info("[fetchAndSaveData] MongoDB 저장 완료");
                    } else {
                        log.warn("[fetchAndSaveData] 저장할 데이터가 없습니다.");
                    }
                    return Mono.just(data);
                });
    }
    
    private Mono<List<DisabledJobofferDTO>> fetchData() {
        String url = API_URL
            + "?serviceKey=" + apiKeyConfig.getEncodedKey()
            + "&pageNo=1"
            + "&numOfRows=100";
        log.info("[fetchData] API 요청 URL: {}", url);
        return webClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(xml -> log.info("[fetchData] API 응답 XML: \n{}", xml))
                .map(this::parseXmlResponse);
    }
    
    private List<DisabledJobofferDTO> parseXmlResponse(String xmlResponse) {
        List<DisabledJobofferDTO> joboffers = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));
            log.info("[parseXmlResponse] XML 파싱 시작");
            NodeList items = document.getElementsByTagName("item");
            log.info("[parseXmlResponse] item 태그 개수: {}", items.getLength());
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
            log.info("[parseXmlResponse] 파싱된 데이터 개수: {}", joboffers.size());
        } catch (Exception e) {
            log.error("[parseXmlResponse] Error parsing XML response", e);
            throw new RuntimeException("Failed to parse XML response", e);
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