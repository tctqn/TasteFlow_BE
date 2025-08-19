package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_batches", indexes = {
        @Index(name = "idx_batch_product", columnList = "product_id"),
        @Index(name = "idx_batch_supplier", columnList = "supplier_id"),
        @Index(name = "idx_batch_warehouse", columnList = "warehouse_id"),
        @Index(name = "idx_batch_unit", columnList = "unit_id"),
        @Index(name = "idx_batch_expiration", columnList = "expiration_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@BatchSize(size = 50)
public class ProductBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long batchId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    @JsonIgnore
    private Warehouse warehouse;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    @JsonIgnore
    private Unit unit;

    @ManyToOne
    @JoinColumn(name = "request_item_id")
    @JsonIgnore
    private WarehouseRequestItem requestItem;

    @Column(name = "quantity", nullable = false)
    @ToString.Include
    private Integer quantity;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiration_date")
    @ToString.Include
    private LocalDate expirationDate;

    @Column(name = "received_date", nullable = false)
    @ToString.Include
    private LocalDateTime receivedDate;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Khuyến nghị: chuyển sang enum (e.g., BatchStatus)
    @Column(name = "status", nullable = false, length = 32)
    @ToString.Include
    private String status;

    @Column(name = "import_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal importPrice;

    @PrePersist
    protected void onCreate() {
        if (receivedDate == null)
            receivedDate = LocalDateTime.now();
        if (importPrice == null)
            importPrice = BigDecimal.ZERO;
    }
}
