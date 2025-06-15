package com.startup.tasteflowbe.dto.response;

import com.startup.tasteflowbe.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponseDTO {
    private Long orderId;
    private String orderCode;
    private String fullName;
    private String phone;
    private String address;
    private String district;
    private String status;
    private PaymentMethod paymentMethod;
    private String note;
    private String deliveryDate;
    private String deliverySlot;
    private BigDecimal totalPrice;
    private String refCode;
    private boolean needInvoice;
    private String invoiceCompanyName;
    private String invoiceEmail;
    private String invoiceTaxCode;
    private String invoiceCompanyAddress;
    private LocalDateTime orderDate;
}
