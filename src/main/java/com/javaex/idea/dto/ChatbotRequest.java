package com.javaex.idea.dto;

import lombok.Data;

@Data
public class ChatbotRequest {
    private String text;
    private String expert_type;
} 