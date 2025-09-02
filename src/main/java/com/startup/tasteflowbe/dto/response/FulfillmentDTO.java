package com.startup.tasteflowbe.dto.response;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FulfillmentDTO {
    private String mode; // "FEFO" (hoặc sau này FIFO/Manual...)
    private List<Item> items;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
        private Long orderItemIdRepresentative; // 1 id đại diện để FE gửi ngược (min id cùng product)
        private Long productId;
        private Integer requestedQty; // tổng SL đặt cho product này trong đơn
        private List<Allocation> allocations; // gộp theo batchId
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Allocation {
        private Long batchId;
        private Integer quantity;
    }
}
