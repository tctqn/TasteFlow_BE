package com.startup.tasteflowbe.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BatchDetailDTO {
    private Long batchId;
    private String productName;
    private Integer quantity;
    private LocalDateTime receivedDate;
    private LocalDate manufactureDate;
    private LocalDate expirationDate;
    private String status;
    private String note;
    private String supplierName;
    private String unitName;
}
