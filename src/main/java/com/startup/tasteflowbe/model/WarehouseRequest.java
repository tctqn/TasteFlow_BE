package com.startup.tasteflowbe.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "warehouse_requests")
@Data
public class WarehouseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @CreationTimestamp
    @Column(name = "request_date", updatable = false)
    private Instant requestDate;

    @Column(nullable = false)
    private String status = "PENDING";

    private String notes;

    @OneToMany(mappedBy = "warehouseRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WarehouseRequestItem> items;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", insertable = false, updatable = false)
    private Warehouse warehouse;

}
