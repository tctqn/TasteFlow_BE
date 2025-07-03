package com.startup.tasteflowbe.dto.request;

import lombok.Data;

@Data
public class UpdateOrderStatusDTO {
    private String status;
    private String notes;
}
