package com.startup.tasteflowbe.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentRequestDTO {
    private String orderCode;
    private String description;
    private Long amount;
    private String cancelUrl;
    private String returnUrl;
    private String signature;
}
