package com.startup.tasteflowbe.dto.response;

import com.startup.tasteflowbe.enums.ReturnStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;

// ReturnRequestResponseDTO.java
@Data
public class ReturnRequestResponseDTO {
    private Long id;
    private String originalOrderCode;
    private Long storeId;
    private Long customerId;
    private ReturnStatus status;
    private String reasonCode;
    private String notes;
    private Instant createdAt;

    private String bankName;
    private String bankAccount;

    private List<ReturnItemResponseDTO> items;
    private List<ReturnAttachmentResponseDTO> attachments;
}

