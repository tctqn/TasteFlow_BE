package com.startup.tasteflowbe.dto.request;

import lombok.Data;

@Data
public class ShippingAddressRequestDTO {

    private String recipientName;

    private String phone;

    private String addressLine;

    private String province;

    private String district;

    private String ward;

    private Boolean isDefault;
}
