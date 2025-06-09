package com.startup.tasteflowbe.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateVoucherRequest {
    private String code;
    private String discountType;
    private String startDate; // ISO-8601 string (yyyy-MM-dd)
    private String endDate;
    private String condition;
    private Integer quantity;
    private String discountAmount;
    private MultipartFile image; // Optional
}
