package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cartitem_user_product", columnNames = {"user_id", "product_id"})
        }
)
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@BatchSize(size = 50) // giúp gom load khi lấy nhiều CartItem
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long cartItemId;

    // to-one -> LAZY để giảm N+1
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // tránh vòng lặp khi serialize; dùng DTO nếu cần trả user
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // tránh vòng lặp khi serialize; dùng DTO nếu cần trả product
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }

    // toString tối giản, không đụng quan hệ để khỏi trigger lazy
    @Override
    public String toString() {
        return "CartItem{id=" + cartItemId + ", qty=" + quantity + "}";
    }

    // tiện ích nhỏ (tuỳ thích)
    public void incrementQuantity(int delta) {
        this.quantity = (this.quantity == null ? 0 : this.quantity) + delta;
    }
}
