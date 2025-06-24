package com.startup.tasteflowbe.dto.request;

import lombok.Data;

@Data
public class InvoiceInfoDTO {
    private String companyName;
    private String email;
    private String taxCode;
    private String companyAddress;
}
