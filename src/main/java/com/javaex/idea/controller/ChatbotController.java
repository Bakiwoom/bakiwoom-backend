package com.javaex.idea.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaex.idea.dto.ChatbotRequest;
import com.javaex.idea.dto.ChatbotResponse;
import com.javaex.idea.dto.ConversationRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.server.url}")
    private String aiServerUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> startChat() {
        logger.info("Starting new chat session");
        try {
            String url = aiServerUrl + "/chat/start";
            ChatbotResponse response = restTemplate.postForObject(url, null, ChatbotResponse.class);

            if (response == null) {
                logger.warn("AI server returned null response");
                return ResponseEntity.noContent().build();
            }

            logger.info("Received welcome message and expert cards from AI server");
            logger.debug("Response Body: {}", objectMapper.writeValueAsString(response));
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            logger.error("Error starting chat: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/expert", produces = MediaType.APPLICATION_JSON_VALUE)
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


    @PostMapping(value = "/conversation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatbotResponse> processConversation(
            @RequestBody ConversationRequest request) {
        String expertType = request.getExpert_type();
        logger.info("Processing conversation with expert type: {}", expertType);
        try {
            String url = aiServerUrl + "/chat/conversation";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ConversationRequest> entity = new HttpEntity<>(request, headers);
            ChatbotResponse response = restTemplate.postForObject(url, entity, ChatbotResponse.class);
            logger.info("Received response from AI server for conversation");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing conversation: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
