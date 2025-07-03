package com.startup.tasteflowbe.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.startup.tasteflowbe.enums.OrderStatus;
import com.startup.tasteflowbe.model.User;

import lombok.Data;

@Data
public class StoreOrderResponseDTO {
    private Long orderId;
    private String orderCode;
    private BigDecimal total_price;
    private OrderStatus status;
    private LocalDateTime order_date;

    private User user;
    List<OrderItemResponseDTO> orderItems;

}
