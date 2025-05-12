package com.javaex.idea.dto;

import lombok.Data;

@Data
public class ChatbotRequest {
    private String text;
    private String expert_type;
    private String user_type;
    public String getExpert_type() {
        return expert_type;
    }
    
    public String getText() {
        return text;
    }
} 