package com.javaex.idea.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    @Value("${ai.server.url}")
    private String aiServerUrl;

    private final RestTemplate restTemplate;

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody ChatbotRequest request) {
        String url = aiServerUrl + "/chatbot/query";
        return ResponseEntity.ok(restTemplate.postForObject(url, request, ChatbotResponse.class));
    }
}

record ChatbotRequest(String text) {}

record PolicyCard(
    String id,
    String title,
    String summary,
    String type,
    String details,
    Source source
) {}

record Source(String url, String name) {}

record ChatbotResponse(
    String answer,
    List<PolicyCard> cards
) {} 