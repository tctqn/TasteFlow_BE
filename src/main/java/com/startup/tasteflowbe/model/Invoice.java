package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "invoices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invoice_order", columnNames = "order_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long invoiceId;

    // Owning side của quan hệ 1-1 với Order (khớp với Order.invoice mappedBy = "order")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @JsonIgnore
    private Order order;

    @Column(name = "invoice_company_name")
    @ToString.Include
    private String invoiceCompanyName;

    @Column(name = "invoice_email")
    private String invoiceEmail;

    @Column(name = "invoice_tax_code")
    private String invoiceTaxCode;

    @Column(name = "invoice_company_address")
    private String invoiceCompanyAddress;

    @Column(name = "invoice_url")
    private String invoiceUrl;

    @Column(name = "issued_at", nullable = false)
    @ToString.Include
    private LocalDateTime issuedAt;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @ToString.Include
    private BigDecimal totalAmount;

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) issuedAt = LocalDateTime.now();
    }
}
