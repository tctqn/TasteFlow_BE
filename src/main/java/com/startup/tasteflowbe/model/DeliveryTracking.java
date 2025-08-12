package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.startup.tasteflowbe.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(
        name = "delivery_trackings",
        indexes = {
                @Index(name = "idx_delivery_tracking_order", columnList = "order_id"),
                @Index(name = "idx_delivery_tracking_status", columnList = "status"),
                @Index(name = "idx_delivery_tracking_updated_at", columnList = "updated_at")
        }
)
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@BatchSize(size = 50)
public class DeliveryTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long trackingId;

    // to-one -> LAZY để tránh N+1
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore // tránh vòng lặp khi serialize; dùng DTO nếu cần trả kèm order
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @ToString.Include
    private OrderStatus status;

    @Column(name = "tracking_number", length = 100)
    @ToString.Include
    private String trackingNumber;

    @Column(name = "carrier", length = 100)
    private String carrier;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "updated_at", nullable = false)
    @ToString.Include
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
