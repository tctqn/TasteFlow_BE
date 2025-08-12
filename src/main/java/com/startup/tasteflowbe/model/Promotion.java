package com.startup.tasteflowbe.model;

import com.startup.tasteflowbe.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "promotions",
        indexes = {
                @Index(name = "idx_promotion_start_end", columnList = "start_date,end_date"),
                @Index(name = "idx_promotion_discount_type", columnList = "discount_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true) // KHÔNG in các quan hệ để tránh vòng lặp & lazy init
@BatchSize(size = 50)
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long promotionId;

    @Column(name = "name", nullable = false, length = 255)
    @ToString.Include
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    @ToString.Include
    private DiscountType discountType;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "start_date", nullable = false)
    @ToString.Include
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    @ToString.Include
    private LocalDateTime endDate;

    @Column(name = "image_url")
    private String imageUrl;

    // ====== Scope áp dụng (Many-to-Many, để LAZY & batch size) ======

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "promotion_products",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @BatchSize(size = 50)
    private Set<Product> applicableProducts = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "promotion_categories",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @BatchSize(size = 50)
    private Set<Category> applicableCategories = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "promotion_stores",
            joinColumns = @JoinColumn(name = "promotion_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    @BatchSize(size = 50)
    private Set<Store> applicableStores = new HashSet<>();

    // ====== Convenience methods (tuỳ chọn) ======
    public boolean isActiveAt(LocalDateTime ts) {
        return (startDate == null || !ts.isBefore(startDate)) &&
                (endDate == null || !ts.isAfter(endDate));
    }

    public boolean isPercentage() { return discountType == DiscountType.PERCENT; }
    public boolean isAmount()     { return discountType == DiscountType.AMOUNT; }
}
