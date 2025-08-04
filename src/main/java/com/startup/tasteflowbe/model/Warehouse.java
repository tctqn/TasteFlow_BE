package com.startup.tasteflowbe.model;

import com.startup.tasteflowbe.enums.Region;
import com.startup.tasteflowbe.enums.WarehouseStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "location", nullable = false, columnDefinition = "TEXT")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false, columnDefinition = "TEXT")
    private Region region;

    @OneToOne
    @JoinColumn(name = "manager_id", referencedColumnName = "user_id", unique = true)
    private User manager;


    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "capacity", nullable = false)
    private Double capacity; // đơn vị m²

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private WarehouseStatus status;

    @Transient
    private Integer totalProduct; // tổng số sản phẩm trong kho

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
