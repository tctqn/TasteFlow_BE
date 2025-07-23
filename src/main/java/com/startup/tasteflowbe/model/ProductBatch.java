package com.startup.tasteflowbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_batches")
@Data
@NoArgsConstructor
public class ProductBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @OneToOne
    @JoinColumn(name = "request_item_id")
    private WarehouseRequestItem requestItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "received_date", nullable = false)
    private LocalDateTime receivedDate = LocalDateTime.now();

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "import_price", nullable = false)
    private BigDecimal importPrice = BigDecimal.ZERO;
}
