package com.startup.tasteflowbe.model;

import com.startup.tasteflowbe.enums.DiscountType;
import com.startup.tasteflowbe.enums.DistributionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "distribution_type", length = 20, nullable = false)
    private DistributionType distributionType; // PUBLIC, PRIVATE, CUSTOMER_ONLY

    @Column(name = "free_shipping", nullable = true)
    private Boolean freeShipping;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_amount", precision = 10, scale = 2, nullable = true)
    private BigDecimal discountAmount;

    @Column(name = "discount_percent", precision = 5, scale = 2, nullable = true)
    private BigDecimal discountPercent;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Builder.Default
    @Column(name = "claimed_count", nullable = false)
    private Integer claimedCount = 0;

    @Column(name = "min_order_amount", precision = 10, scale = 2, nullable = true)
    private BigDecimal minOrderAmount;

    @Column(name = "max_per_user", nullable = false)
    private Integer maxPerUser;

    @Column(nullable = false)
    private boolean isStackable;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "voucher_products",
            joinColumns = @JoinColumn(name = "voucher_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> applicableProducts = new HashSet<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "voucher_categories",
            joinColumns = @JoinColumn(name = "voucher_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> applicableCategories = new HashSet<>();
}
