package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_orderitem_order", columnList = "order_id"),
                @Index(name = "idx_orderitem_product", columnList = "product_id"),
                @Index(name = "idx_orderitem_product_unit", columnList = "product_unit_id"),
                @Index(name = "idx_orderitem_batch", columnList = "batch_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@BatchSize(size = 50)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long orderItemId;

    // to-one -> LAZY để tránh N+1 khi duyệt danh sách
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore // tránh vòng lặp khi serialize; trả qua DTO nếu cần
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // thường trả DTO cho client thay vì entity
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_unit_id", nullable = false)
    @JsonIgnore
    private ProductUnit productUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    @JsonIgnore
    private ProductBatch batch; // có thể null

    @Column(name = "quantity", nullable = false)
    @ToString.Include
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @ToString.Include
    private BigDecimal price;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount; // sẽ chuẩn hoá về ZERO ở @PrePersist

    @Column(name = "quantity_in_base", nullable = false)
    private Integer quantityInBase;

    @PrePersist
    protected void onCreate() {
        if (discount == null) discount = BigDecimal.ZERO;
    }
}
