package com.javaex.idea.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ConversationRequest {
    private List<Map<String, String>> messages;
    private String expert_type;
    private String user_type;
    public String getExpert_type() {
        return expert_type;
    }

    public List<Map<String, String>> getMessages() {
        return messages;
    }
}