package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "warehouse_request_items")
@Data
public class WarehouseRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_item_id")
    private Integer requestItemId;

    @Column(name = "product_unit_id", nullable = false)
    private Integer productUnitId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "fulfilled_quantity")
    private Integer fulfilledQuantity = 0;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    @JsonIgnore // Tránh vòng lặp vô hạn khi serialize JSON
    private WarehouseRequest warehouseRequest;
}
