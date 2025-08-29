package com.startup.tasteflowbe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_requests")
@Data
@NoArgsConstructor
public class StoreRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "request_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime requestDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "storeRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<StoreRequestItem> items;

    @PrePersist
    protected void onCreate() {
        requestDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
}