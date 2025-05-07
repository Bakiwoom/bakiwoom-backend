package com.javaex.idea.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatbotResponse {
    private String answer;
    private List<PolicyCard> cards;
    private List<ExpertCard> action_cards;

    @Data
    public static class PolicyCard {
        private String id;
        private String title;
        private String summary;
        private String type;
        private String details;
        private String imageUrl;
        private Source source;
    }

    @Data
    public static class ExpertCard {
        private String id;
        private String title;
        private String expert_type;
        private String description;
        private String icon;
    }

    @Data
    public static class Source {
        private String url;
        private String name;
    }
} 