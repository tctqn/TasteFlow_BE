package com.startup.tasteflowbe.dto;

import lombok.Data;

@Data
public class PayOSWebhookDTO {
    private Boolean success;
    private WebhookData data;
    private String event;
    private String timestamp;
    private String signature;

    @Data
    public static class WebhookData {
        private Long orderCode;
        private Integer amount;
        private String description;
        private String state;
        private String transactionId;
        private String transTime;
    }
}
