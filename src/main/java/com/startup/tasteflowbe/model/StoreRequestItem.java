package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_request_items")
@Data
@NoArgsConstructor
public class StoreRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_item_id")
    private Long requestItemId;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    @JsonBackReference
    private StoreRequest storeRequest;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

}
