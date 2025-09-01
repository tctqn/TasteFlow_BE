package com.startup.tasteflowbe.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReturnRequestRequestDTO {
    @NotBlank
    private String originalOrderCode;
    @NotNull
    private Long storeId;
    private Long customerId;
    private String bankName;
    private String bankAccount;


    @NotBlank private String reasonCode; // "Sản phẩm bị hỏng, vỡ"...
    private String notes;

    @Size(min = 1) @Valid
    private List<ReturnItemRequestDTO> items;
}
