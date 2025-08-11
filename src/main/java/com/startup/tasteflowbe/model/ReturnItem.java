package com.startup.tasteflowbe.model;

import com.startup.tasteflowbe.enums.ItemCondition;
import com.startup.tasteflowbe.enums.ReturnResolution;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "return_items")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReturnItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="return_id")
    private ReturnRequest returnRequest;

    private Long orderItemId;
    private Long productId;

    private BigDecimal quantity;
    @Enumerated(EnumType.STRING)
    private ItemCondition condition;

    @Enumerated(EnumType.STRING)
    private ReturnResolution resolution;

    private BigDecimal refundAmount;

}

