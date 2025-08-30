package com.startup.tasteflowbe.model;

import com.startup.tasteflowbe.enums.MovementType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "stock_movements")
@Data
@NoArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Long movementId;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private ProductBatch batch;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

    @Column(name = "note")
    private String note;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private StoreRequest storeRequest;

    @Override
    public String toString() {
        return "StockMovement{" +
                "movementId=" + movementId +
                ", warehouse=" + (warehouse != null ? warehouse.getWarehouseId() : null) +
                ", store=" + (store != null ? store.getStoreId() : null) +
                ", product=" + (product != null ? product.getProductId() : null) +
                ", batch=" + (batch != null ? batch.getBatchId() : null) +
                ", movementType=" + movementType +
                ", quantity=" + quantity +
                ", movementDate=" + movementDate +
                ", note='" + note + '\'' +
                ", storeRequest=" + (storeRequest != null ? storeRequest.getRequestId() : null) +
                '}';
    }
}
