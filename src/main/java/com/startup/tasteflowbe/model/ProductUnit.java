package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;

@Entity
@Table(
        name = "product_units",
        indexes = {
                @Index(name = "idx_productunit_product", columnList = "product_id"),
                @Index(name = "idx_productunit_unit", columnList = "unit_id"),
                @Index(name = "idx_productunit_sku", columnList = "sku")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_productunit_sku", columnNames = {"sku"}),
                @UniqueConstraint(name = "uk_productunit_product_unit", columnNames = {"product_id", "unit_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@BatchSize(size = 50)
public class ProductUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_unit_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long productUnitId;

    // to-one -> LAZY để tránh N+1
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    @JsonIgnore
    private Unit unit;

    @Column(name = "sku", nullable = false, length = 50)
    @ToString.Include
    private String sku;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "qr_code_url", columnDefinition = "TEXT")
    private String qrCodeUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Số lượng đơn vị cơ sở/1 đơn vị này (ví dụ: 1 thùng = 24 lon) */
    @Column(name = "conversion_rate")
    @ToString.Include
    private Integer conversionRate;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @ToString.Include
    private BigDecimal price;

    @Column(name = "is_base_unit")
    private Boolean isBaseUnit;


    @PrePersist
    protected void onCreate() {
        if (isBaseUnit == null) isBaseUnit = Boolean.FALSE;
        if (conversionRate == null) conversionRate = 1;
    }
}
