package com.startup.tasteflowbe.model;

import com.startup.tasteflowbe.enums.OrderStatus;
import com.startup.tasteflowbe.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_code", unique = true)
    private String orderCode;

    // Thông tin user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Thông tin người nhận
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address", nullable = false)
    private String address;

    // Thông tin thanh toán
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "ref_code")
    private String refCode;

    @Column(name = "note")
    private String note;

    // Thông tin giao hàng
    @Column(name = "delivery_date", nullable = false)
    private String deliveryDate;

    @Column(name = "delivery_slot", nullable = false)
    private String deliverySlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    // Có xuất hóa đơn hay không
    @Column(name = "need_invoice", nullable = false)
    private boolean needInvoice;

    // Thông tin voucher
    @ManyToMany
    @JoinTable(
            name = "order_vouchers",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "voucher_id")
    )
    private List<Voucher> vouchers;

    @Column(name = "voucher_discount", precision = 10, scale = 2)
    private BigDecimal voucherDiscount;

    // Thông tin hệ thống khác
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "shipping_address_id")
    private ShippingAddress shippingAddress;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Invoice invoice;
}
