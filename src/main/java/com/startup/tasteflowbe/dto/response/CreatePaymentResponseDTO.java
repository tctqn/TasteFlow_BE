package com.startup.tasteflowbe.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentResponseDTO {
    private String orderCode;
    private String status;
    private Long amount;
    private String checkoutUrl;
    private String qrCode;
    private String bankName;          // Ngân hàng: BIDV
    private String accountName;       // Tên chủ tài khoản: NGUYEN QUANG TUYEN
    private String accountNumber;     // Số tài khoản: V3CAS8854795351
    private String paymentNote;       // Nội dung chuyển khoản: CS6SCL119M4 Thanh toán đơn hàng 8
}

