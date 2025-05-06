package com.javaex.idea.controller;

import com.javaex.idea.dto.ChatbotRequest;
import com.javaex.idea.dto.ChatbotResponse;
import com.javaex.idea.dto.ConversationRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);
    private final RestTemplate restTemplate;
    
    @Value("${ai.server.url}")
    private String aiServerUrl;

    @PostMapping("/start")
    public ResponseEntity<ChatbotResponse> startChat() {
        logger.info("Starting new chat session");
        try {
            String url = aiServerUrl + "/chat/start";
            ChatbotResponse response = restTemplate.postForObject(url, null, ChatbotResponse.class);
            logger.info("Received welcome message and expert cards from AI server");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error starting chat: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/expert")
    public ResponseEntity<ChatbotResponse> queryExpert(
            @RequestBody ChatbotRequest request) {
        String expertType = request.getExpert_type();
        logger.info("Received query for expert {}: {}", expertType, request.getText());
        try {
            String url = aiServerUrl + "/chat/expert";
            ChatbotResponse response = restTemplate.postForObject(url, request, ChatbotResponse.class);
            logger.info("Received response from AI server for expert {}", expertType);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing expert query: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/conversation")
    public ResponseEntity<ChatbotResponse> processConversation(
            @RequestBody ConversationRequest request) {
        String expertType = request.getExpert_type();
        logger.info("Processing conversation with expert type: {}", expertType);
        try {
            String url = aiServerUrl + "/chat/conversation";
            ChatbotResponse response = restTemplate.postForObject(url, request, ChatbotResponse.class);
            logger.info("Received response from AI server for conversation");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing conversation: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 