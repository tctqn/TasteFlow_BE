package com.startup.tasteflowbe.dto.response;

import com.startup.tasteflowbe.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private BigDecimal shippingFee;
    private BigDecimal finalPrice;
    private Integer pointsApplied;
    private Integer pointsUsed;
    private String refCode;
    private boolean needInvoice;
    private String invoiceCompanyName;
    private String invoiceEmail;
    private String invoiceTaxCode;
    private String invoiceCompanyAddress;
    private String invoiceUrl;
    private LocalDateTime invoiceIssuedAt;
    private LocalDateTime orderDate;
    private StoreResponseDTO store;
    private List<OrderItemResponseDTO> items;
}
