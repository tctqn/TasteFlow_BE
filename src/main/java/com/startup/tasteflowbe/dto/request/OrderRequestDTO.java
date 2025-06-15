package com.startup.tasteflowbe.dto.request;

import com.startup.tasteflowbe.dto.CartItemDTO;
import com.startup.tasteflowbe.dto.InvoiceInfoDTO;
import com.startup.tasteflowbe.enums.OrderStatus;
import com.startup.tasteflowbe.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequestDTO {

    private List<CartItemDTO> cartItems;

    // Thông tin giá trị đơn hàng
    private BigDecimal totalPrice;

    // Thông tin người nhận
    private String fullName;
    private String phone;
    private String address;

    // Thông tin giao hàng
    private String deliveryDate;
    private String deliverySlot;

    // Thông tin thanh toán
    private String note;
    private String refCode;
    private PaymentMethod paymentMethod;

    // Thông tin xuất hóa đơn
    private boolean needInvoice;
    private InvoiceInfoDTO invoiceInfo;

    // Các thông tin liên quan đến hệ thống
    private Long storeId;
    private Long shippingAddressId;
    private List<Long> voucherIds;
}
