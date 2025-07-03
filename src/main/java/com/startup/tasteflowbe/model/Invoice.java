package com.startup.tasteflowbe.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "invoices",
        uniqueConstraints = @UniqueConstraint(columnNames = "order_id")
)
@Data
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "invoice_company_name")
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
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
}
