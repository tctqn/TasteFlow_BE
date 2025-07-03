package com.startup.tasteflowbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipping_addresses")
@Data
@NoArgsConstructor
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_name", length = 100, nullable = false)
    private String recipientName;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "address_line", columnDefinition = "TEXT", nullable = false)
    private String addressLine;

    @Column(name = "province", length = 100, nullable = false)
    private String province;

    @Column(name = "district", length = 100, nullable = false)
    private String district;

    @Column(name = "ward", length = 100, nullable = false)
    private String ward;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
