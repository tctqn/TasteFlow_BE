package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.startup.tasteflowbe.enums.OrderStatus;
import com.startup.tasteflowbe.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
                @Index(name = "idx_order_user", columnList = "user_id"),
                @Index(name = "idx_order_store", columnList = "store_id"),
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_date", columnList = "order_date")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_code", columnNames = "order_code")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Order {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "order_id")
        @EqualsAndHashCode.Include
        @ToString.Include
        private Long orderId;

        @Column(name = "order_code", unique = true)
        @ToString.Include
        private String orderCode;

        @ManyToOne
        @JoinColumn(name = "user_id")
        @JsonIgnore
        private User user;

        // Người nhận
        @Column(name = "full_name", nullable = false)
        @ToString.Include
        private String fullName;

        @Column(name = "phone", nullable = false)
        private String phone;

        @Column(name = "address", nullable = false)
        private String address;

        // Thanh toán
        @Enumerated(EnumType.STRING)
        @Column(name = "payment_method", nullable = false, length = 32)
        private PaymentMethod paymentMethod;

        @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
        private BigDecimal totalPrice;

        @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
        private BigDecimal shippingFee = BigDecimal.ZERO;

        @Column(name = "final_price", nullable = false, precision = 10, scale = 2)
        @ToString.Include
        private BigDecimal finalPrice;

        @Column(name = "ref_code")
        private String refCode;

        @Column(name = "note")
        private String note;

        // Giao hàng
        @Column(name = "delivery_date")
        private String deliveryDate;

        @Column(name = "delivery_slot", nullable = false)
        private String deliverySlot;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 32)
        @ToString.Include
        private OrderStatus status = OrderStatus.PENDING;

        @Column(name = "need_invoice", nullable = false)
        private boolean needInvoice;

        @ManyToMany
        @JoinTable(name = "order_vouchers", joinColumns = @JoinColumn(name = "order_id"), inverseJoinColumns = @JoinColumn(name = "voucher_id"))
        @BatchSize(size = 50)
        private List<Voucher> vouchers;

        @Column(name = "voucher_discount", precision = 10, scale = 2)
        private BigDecimal voucherDiscount;

        // Hệ thống
        @Column(name = "order_date", nullable = false)
        private LocalDateTime orderDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        @ManyToOne
        @JoinColumn(name = "shipping_address_id")
        @JsonIgnore
        private ShippingAddress shippingAddress;

        @ManyToOne
        @JoinColumn(name = "store_id")
        private Store store;

        @OneToMany(mappedBy = "order")
        @BatchSize(size = 50)
        private List<OrderItem> orderItems;

        @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
        @JsonIgnore
        private Invoice invoice;

        @PrePersist
        protected void prePersist() {
                if (orderDate == null)
                        orderDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        }
}
