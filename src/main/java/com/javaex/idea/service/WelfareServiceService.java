package com.javaex.idea.service;

import com.javaex.config.ApiKeyConfig;
import com.javaex.idea.dto.*;
import com.javaex.idea.repository.WelfareServiceListRepository;
import com.javaex.idea.repository.WelfareServiceDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;
import java.util.*;
import reactor.core.publisher.Flux;
import java.lang.reflect.Field;
import java.net.URI;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Duration;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
// import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class WelfareServiceService {

    private final WebClient webClient;
    private final ApiKeyConfig apiKeyConfig;
    private final WelfareServiceListRepository listRepository;
    private final WelfareServiceDetailRepository detailRepository;

    private static final String BASE_URL = "https://apis.data.go.kr/B554287/NationalWelfareInformationsV001";

    // 1. 복지서비스 목록 전체 갱신 (여러 페이지 반복)
    public Mono<List<WelfareServiceListDTO>> fetchAndSaveAllList() {
        log.info("[Service] fetchAndSaveAllList 진입");
        int numOfRows = 100;
        int pageNo = 1;
        List<WelfareServiceListDTO> allData = new ArrayList<>();
        return fetchListData(pageNo, numOfRows)
            .flatMap(firstPage -> {
                log.info("[Service] fetchAndSaveAllList 1페이지 데이터 수신");
                Object totalCountObj = firstPage.get("totalCount");
                int totalCount;
                if (totalCountObj instanceof Integer) {
                    totalCount = (Integer) totalCountObj;
                } else if (totalCountObj instanceof String) {
                    totalCount = Integer.parseInt((String) totalCountObj);
                } else {
                    totalCount = 0;
                }
                log.info("[Service] totalCount: {}", totalCount);
                int totalPages = (int) Math.ceil((double) totalCount / numOfRows);
                Object listObj = firstPage.get("list");
                if (listObj instanceof List<?>) {
                    for (Object o : (List<?>) listObj) {
                        if (o instanceof WelfareServiceListDTO) {
                            allData.add((WelfareServiceListDTO) o);
                        }
                    }
                }
                log.info("[Service] 1페이지 파싱 후 데이터 개수: {}", allData.size());
                
                // 순차적으로 처리
                return Flux.range(2, totalPages - 1)
                    .flatMap(page -> 
                        Mono.delay(Duration.ofSeconds(1))
                            .then(fetchListData(page, numOfRows))
                    )
                    .doOnNext(map -> {
                        Object mapListObj = map.get("list");
                        if (mapListObj instanceof List<?>) {
                            for (Object o : (List<?>) mapListObj) {
                                if (o instanceof WelfareServiceListDTO) {
                                    allData.add((WelfareServiceListDTO) o);
                                }
                            }
                        }
                        log.info("[Service] 추가 페이지 데이터 누적, 현재 개수: {}", allData.size());
                    })
                    .then(Mono.defer(() -> {
                        log.info("[Service] 최종 저장 데이터 개수: {}", allData.size());
                        listRepository.saveAll(allData);
                        return Mono.just(allData);
                    }));
            })
            .doOnError(e -> log.error("[Service] fetchAndSaveAllList 에러", e));
    }

    // 2. 목록 데이터 1페이지 파싱
    private Mono<Map<String, Object>> fetchListData(int pageNo, int numOfRows) {
        String url = BASE_URL + "/NationalWelfarelistV001"
            + "?serviceKey=" + apiKeyConfig.getEncodedKey()
            + "&callTp=l"
            + "&pageNo=" + pageNo
            + "&numOfRows=" + numOfRows
            + "&srchKeyCode=003";
        log.info("[Service] fetchListData API 요청: {}", url);
        return webClient.get()
            .uri(URI.create(url))
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(xml -> log.info("[Service] fetchListData 응답 XML 일부: {}", xml != null && xml.length() > 200 ? xml.substring(0, 200) + "..." : xml))
            .map(xml -> parseListXml(xml))
            .doOnError(e -> log.error("[Service] fetchListData 에러", e));
    }

    // 3. 목록 XML 파싱
    private Map<String, Object> parseListXml(String xml) {
        log.info("[Service] parseListXml 진입");
        List<WelfareServiceListDTO> list = new ArrayList<>();
        int totalCount = 0;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            NodeList totalCountNode = document.getElementsByTagName("totalCount");
            if (totalCountNode.getLength() > 0) {
                String totalCountStr = totalCountNode.item(0).getTextContent();
                log.info("[Service] parseListXml totalCountStr: {}", totalCountStr);
                try {
                    totalCount = Integer.parseInt(totalCountStr);
                } catch (Exception e) {
                    log.warn("[Service] totalCount 파싱 실패: {}", totalCountStr);
                }
            }
            NodeList items = document.getElementsByTagName("servList");
            log.info("[Service] parseListXml servList 개수: {}", items.getLength());
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                WelfareServiceListDTO dto = new WelfareServiceListDTO();
                dto.setServId(getElementValue(item, "servId"));
                dto.setInqNum(getElementValue(item, "inqNum"));
                dto.setIntrsThemaArray(getElementValue(item, "intrsThemaArray"));
                dto.setJurMnofNm(getElementValue(item, "jurMnofNm"));
                dto.setJurOrgNm(getElementValue(item, "jurOrgNm"));
                dto.setLifeArray(getElementValue(item, "lifeArray"));
                dto.setOnapPsbltYn(getElementValue(item, "onapPsbltYn"));
                dto.setRprsCtadr(getElementValue(item, "rprsCtadr"));
                dto.setServDgst(getElementValue(item, "servDgst"));
                dto.setServDtlLink(getElementValue(item, "servDtlLink"));
                dto.setServNm(getElementValue(item, "servNm"));
                dto.setSprtCycNm(getElementValue(item, "sprtCycNm"));
                dto.setSrvPvsnNm(getElementValue(item, "srvPvsnNm"));
                dto.setSvcfrstRegTs(getElementValue(item, "svcfrstRegTs"));
                dto.setTrgterIndvdlArray(getElementValue(item, "trgterIndvdlArray"));
                list.add(dto);
            }
            log.info("[Service] parseListXml 파싱된 목록 개수: {}", list.size());
        } catch (Exception e) {
            log.error("[Service] parseListXml 에러", e);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("list", list);
        return result;
    }

    // 4. 상세 데이터 조회 및 저장
    public Mono<WelfareServiceDetailDTO> fetchAndSaveDetail(String servId) {
        log.info("[Service] fetchAndSaveDetail 진입 - servId: {}", servId);
        
        String url = BASE_URL + "/NationalWelfaredetailedV001"
            + "?serviceKey=" + apiKeyConfig.getEncodedKey()
            + "&callTp=D"
            + "&servId=" + servId;
            
        log.info("[Service] API 요청 URL: {}", url);
        
        return webClient.get()
            .uri(URI.create(url))
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(xml -> log.info("[Service] API 응답 XML: \n{}", xml))
            .map(xml -> {
                try {
                    WelfareServiceDetailDTO dto = parseDetailXml(xml);
                    if (dto == null) {
                        throw new RuntimeException("상세 데이터를 찾을 수 없습니다.");
                    }
                    return dto;
                } catch (Exception e) {
                    log.error("[Service] 상세 데이터 파싱 실패 - servId: {}", servId, e);
                    throw new RuntimeException("상세 데이터 파싱 실패", e);
                }
            })
            .flatMap(dto -> {
                log.info("[Service] 상세 데이터 저장 시작 - servId: {}", servId);
                return Mono.fromCallable(() -> detailRepository.save(dto))
                    .doOnSuccess(saved -> log.info("[Service] 상세 데이터 저장 완료 - servId: {}", servId))
                    .doOnError(e -> log.error("[Service] 상세 데이터 저장 실패 - servId: {}", servId, e));
            })
            .doOnError(e -> log.error("[Service] 상세 데이터 조회 실패 - servId: {}", servId, e));
    }

    // 5. 상세 XML 파싱 (하위 DTO까지)
    private WelfareServiceDetailDTO parseDetailXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            
            // 에러 응답 체크
            NodeList errorNodes = document.getElementsByTagName("cmmMsgHeader");
            if (errorNodes.getLength() > 0) {
                Element errorHeader = (Element) errorNodes.item(0);
                String errorMsg = getElementValue(errorHeader, "errMsg");
                String returnAuthMsg = getElementValue(errorHeader, "returnAuthMsg");
                if (errorMsg != null && !errorMsg.isEmpty()) {
                    log.error("[parseDetailXml] API 에러 응답 - errMsg: {}, returnAuthMsg: {}", errorMsg, returnAuthMsg);
                    throw new RuntimeException("API 에러: " + errorMsg + " - " + returnAuthMsg);
                }
            }

            Element wantedDtl = (Element) document.getElementsByTagName("wantedDtl").item(0);
            if (wantedDtl == null) {
                log.error("[parseDetailXml] wantedDtl 태그가 없음. 원본 XML: {}", xml);
                throw new RuntimeException("wantedDtl 태그를 찾을 수 없습니다.");
            }

            // resultCode 체크
            String resultCode = getElementValue(wantedDtl, "resultCode");
            String resultMessage = getElementValue(wantedDtl, "resultMessage");
            if (resultCode != null && !resultCode.equals("0")) {
                log.error("[parseDetailXml] API 응답 에러 - resultCode: {}, resultMessage: {}", resultCode, resultMessage);
                throw new RuntimeException("API 응답 에러: " + resultMessage);
            }

            WelfareServiceDetailDTO dto = new WelfareServiceDetailDTO();
            dto.setServId(getElementValue(wantedDtl, "servId"));
            dto.setServNm(getElementValue(wantedDtl, "servNm"));
            dto.setJurMnofNm(getElementValue(wantedDtl, "jurMnofNm"));
            dto.setTgtrDtlCn(getElementValue(wantedDtl, "tgtrDtlCn"));
            dto.setSlctCritCn(getElementValue(wantedDtl, "slctCritCn"));
            dto.setAlwServCn(getElementValue(wantedDtl, "alwServCn"));
            dto.setCrtrYr(getElementValue(wantedDtl, "crtrYr"));
            dto.setRprsCtadr(getElementValue(wantedDtl, "rprsCtadr"));
            dto.setWlfareInfoOutlCn(getElementValue(wantedDtl, "wlfareInfoOutlCn"));
            dto.setSprtCycNm(getElementValue(wantedDtl, "sprtCycNm"));
            dto.setSrvPvsnNm(getElementValue(wantedDtl, "srvPvsnNm"));
            dto.setLifeArray(getElementValue(wantedDtl, "lifeArray"));
            dto.setTrgterIndvdlArray(getElementValue(wantedDtl, "trgterIndvdlArray"));
            dto.setIntrsThemaArray(getElementValue(wantedDtl, "intrsThemaArray"));
            dto.setApplmetList(parseSubList(wantedDtl, "applmetList", ApplmetItemDTO.class));
            dto.setInqplCtadrList(parseSubList(wantedDtl, "inqplCtadrList", InqplCtadrItemDTO.class));
            dto.setInqplHmpgReldList(parseSubList(wantedDtl, "inqplHmpgReldList", InqplHmpgReldItemDTO.class));
            dto.setBasfrmList(parseSubList(wantedDtl, "basfrmList", BasfrmItemDTO.class));
            dto.setBaslawList(parseSubList(wantedDtl, "baslawList", BaslawItemDTO.class));
            dto.setResultCode(resultCode);
            dto.setResultMessage(resultMessage);
            return dto;
        } catch (Exception e) {
            log.error("[parseDetailXml] XML 파싱 오류", e);
            throw new RuntimeException("XML 파싱 오류: " + e.getMessage(), e);
        }
    }

    // 6. 하위 리스트 파싱 (실제 XML 구조에 맞게 반복 태그명만 받음)
    private <T> List<T> parseSubList(Element parent, String itemTag, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        try {
            NodeList items = parent.getElementsByTagName(itemTag);
            for (int i = 0; i < items.getLength(); i++) {
                Element itemElem = (Element) items.item(i);
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String value = getElementValue(itemElem, field.getName());
                    field.set(obj, value);
                }
                result.add(obj);
            }
        } catch (Exception e) {
            log.error("[parseSubList] 하위 리스트 파싱 오류", e);
        }
        return result;
    }

    // 7. 공통 엘리먼트 값 추출
    private String getElementValue(Element parent, String tagName) {
        if (parent == null) return null;
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    public List<WelfareServiceListDTO> getAllList() {
        return listRepository.findAll();
    }

    public List<WelfareServiceListDTO> searchByKeywords(String keyword) {
        String[] keywords = keyword.trim().split("\\s+");
        List<WelfareServiceListDTO> allServices = listRepository.findAll();

        // 각 서비스별 일치 개수 계산
        List<Pair<WelfareServiceListDTO, Integer>> scored = new ArrayList<>();
        for (WelfareServiceListDTO service : allServices) {
            String titleNoSpace = service.getServNm().replaceAll("\\s+", "");
            int matchCount = 0;
            for (String kw : keywords) {
                String kwNoSpace = kw.replaceAll("\\s+", "");
                if (titleNoSpace.contains(kwNoSpace)) {
                    matchCount++;
                }
            }
            if (matchCount > 0) { // 한 개 이상 일치하는 경우만
                scored.add(Pair.of(service, matchCount));
            }
        }

        // 일치 개수(정확도) 기준 내림차순 정렬
        scored.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // 결과만 추출
        List<WelfareServiceListDTO> results = scored.stream()
            .map(Pair::getKey)
            .collect(Collectors.toList());

        return results;
    }

    public Mono<WelfareServiceDetailDTO> getDetail(String servId) {
        log.info("[Service] getDetail 진입 - servId: {}", servId);
        return Mono.fromCallable(() -> detailRepository.findById(servId))
            .flatMap(optional -> {
                if (optional.isPresent()) {
                    log.info("[Service] DB에서 상세 데이터 조회 성공 - servId: {}", servId);
                    return Mono.just(optional.get());
                } else {
                    log.info("[Service] DB에 상세 데이터 없음, API에서 조회 시도 - servId: {}", servId);
                    return fetchAndSaveDetail(servId);
                }
            })
            .doOnError(e -> log.error("[Service] getDetail 에러 - servId: {}", servId, e));
    }

    // 매일 새벽 3시에 실행
    @Scheduled(cron = "0 0 3 * * *")
    public void batchSaveMissingDetails() {
        List<String> allServIds = listRepository.findAllServIds();
        List<String> detailServIds = detailRepository.findAllServIds();
        Set<String> missingServIds = new HashSet<>(allServIds);
        missingServIds.removeAll(detailServIds);
        log.info("[Batch] 상세 없는 servId 개수: {}", missingServIds.size());
        
        for (String servId : missingServIds) {
            try {
                // 순차적으로 처리하고 1초 대기
                fetchAndSaveDetail(servId).block();
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("[Batch] 상세 저장 실패 - servId: {}", servId, e);
            }
        }
    }

//    @PostConstruct
//    public void testBatchSaveMissingDetails() {
//        batchSaveMissingDetails();
//    }
}
