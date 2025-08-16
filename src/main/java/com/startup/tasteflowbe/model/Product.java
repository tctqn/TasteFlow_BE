package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_name", columnList = "name"),
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@BatchSize(size = 50)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long productId;

    @Column(name = "name", nullable = false, length = 255)
    @ToString.Include
    private String name;

    // to-one -> LAZY để tránh N+1
    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // to-many -> LAZY (mặc định), không serialize trực tiếp để tránh vòng lặp
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JsonIgnore
    private List<ProductUnit> productUnits;

    @Column(name = "isDraft")
    private Boolean isDraft = Boolean.FALSE;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isDraft == null) isDraft = Boolean.FALSE;
    }

    // toString tối giản đã đủ (đã dùng @ToString.Include ở trên)
}
