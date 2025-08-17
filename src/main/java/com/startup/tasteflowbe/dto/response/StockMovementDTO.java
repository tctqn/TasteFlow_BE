package com.startup.tasteflowbe.dto.response;

import com.startup.tasteflowbe.enums.MovementType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StockMovementDTO {
    private Long movementId;
    private ProductInfo product;
    private BatchInfo batch;
    private MovementType movementType;
    private Integer quantity;
    private LocalDateTime movementDate;
    private WarehouseInfo warehouse;
    private StoreInfo store;
    private String note;

    @Data
    public static class ProductInfo {
        private Long productId;
        private String name;
    }

    @Data
    public static class BatchInfo {
        private Long batchId;
    }

    @Data
    public static class WarehouseInfo {
        private Long warehouseId;
        private String name;
    }

    @Data
    public static class StoreInfo {
        private Long storeId;
        private String name;
    }
}
