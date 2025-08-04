package com.startup.tasteflowbe.dto.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class StoreOrderDTO {
    private Long store_id;
    private String full_name;
    private String phone;
    private String address;
    private String delivery_date;
    private String delivery_slot;
    private String payment_method;
    private String status;
    private Boolean need_invoice;
    private BigDecimal total_price;
    private BigDecimal voucher_discount;
    private String note;
    private BigDecimal shipping_fee;
    private BigDecimal final_price;
    private List<OrderItemDTO> order_items;

    @Data
    public static class OrderItemDTO {
        private Long product_id;
        private Long product_unit_id;
        private BigDecimal price;
        private BigDecimal discount;
        private Integer quantity;
        private Integer quantity_in_base;
    }
}
