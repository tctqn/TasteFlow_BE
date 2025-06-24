package com.startup.tasteflowbe.dto;

import lombok.Data;

@Data
public class PayOSWebhookDTO {
    private String code;
    private String desc;
    private WebhookData data;
    private String signature;

    @Data
    public static class WebhookData {
        private Long orderCode;
        private Integer amount;
        private String description;
        private String accountNumber;
        private String reference;
        private String transactionDateTime;
        private String paymentLinkId;
        private String code;
        private String desc;
        private String counterAccountBankId;
        private String counterAccountBankName;
        private String counterAccountName;
        private String counterAccountNumber;
        private String virtualAccountName;
        private String virtualAccountNumber;
        private String currency;
    }
}
