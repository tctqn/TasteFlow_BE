package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import java.io.Serializable;

@Entity
@Table(
        name = "inventories",
        indexes = {
                @Index(name = "idx_inventory_product", columnList = "product_id"),
                @Index(name = "idx_inventory_batch", columnList = "batch_id"),
                @Index(name = "idx_inventory_warehouse", columnList = "warehouse_id"),
                @Index(name = "idx_inventory_store", columnList = "store_id")
        }
        // Tuỳ mô hình dữ liệu, bạn có thể đặt UNIQUE phù hợp, ví dụ:
        // uniqueConstraints = {
        //   @UniqueConstraint(name = "uk_inv_wh_product_batch",
        //       columnNames = {"warehouse_id", "product_id", "batch_id"}),
        //   @UniqueConstraint(name = "uk_inv_store_product_batch",
        //       columnNames = {"store_id", "product_id", "batch_id"})
        // }
)
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@BatchSize(size = 50)
public class Inventory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long inventoryId;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    @JsonIgnore
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @JsonIgnore
    private Store store;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnore
    private ProductBatch batch;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 10;
}
