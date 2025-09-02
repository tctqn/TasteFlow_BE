package com.startup.tasteflowbe.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateOrderStatusDTO {
    private String status;
    private String notes;

    // Thêm field mới
    private FulfillmentDTO fulfillment;

    @Data
    public static class FulfillmentDTO {
        private String mode;
        private List<FulfillmentItemDTO> items;
    }

    @Data
    public static class FulfillmentItemDTO {
        private Long orderItemId;
        private Long productId;
        private Integer requestedQty;
        private List<AllocationDTO> allocations;
    }

    @Data
    public static class AllocationDTO {
        private Long batchId;
        private Integer quantity;
    }
}
