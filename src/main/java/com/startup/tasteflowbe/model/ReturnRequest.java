package com.startup.tasteflowbe.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.startup.tasteflowbe.enums.ReturnStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "return_requests")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;
    private String originalOrderCode;
    private Long customerId;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING;

    private String bankName;
    private String bankAccount;

    private String reasonCode;
    private String notes;

    private Long createdBy;
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<ReturnAttachment> attachments = new ArrayList<>();
}
