package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_order", columnList = "order_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_date", columnList = "payment_date")
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
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long paymentId;

    // to-one -> LAZY để tránh N+1 khi duyệt payment list
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order; // Một Order có thể có nhiều lần Payment

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @ToString.Include
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod; // gợi ý: chuyển sang enum

    @Column(name = "status", nullable = false, length = 50)
    @ToString.Include
    private String status; // gợi ý: chuyển sang enum ('PENDING','PAID','FAILED','REFUNDED')

    @Column(name = "payment_date", nullable = false)
    @ToString.Include
    private LocalDateTime paymentDate;

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) paymentDate = LocalDateTime.now();
    }
}
