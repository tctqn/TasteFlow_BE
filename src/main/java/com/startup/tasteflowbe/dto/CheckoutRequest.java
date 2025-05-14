package com.startup.tasteflowbe.dto;

import lombok.Data;
import java.util.List;

@Data
public class CheckoutRequest {
    private List<Long> cartItemIds;
    private List<Long> voucherIds;
    private Long shippingAddressId;
    private Long storeId;
}
