package com.startup.tasteflowbe.dto.response;

import com.startup.tasteflowbe.enums.Region;
import com.startup.tasteflowbe.enums.StoreStatus;
import lombok.Data;

@Data
public class StoreResponseDTO {
    private Long storeId;
    private String name;
    private String address;
    private String contactInfo;
    private String businessHours;
    private Region region;
    private StoreStatus status;
    private Long managerId;
    private String province;
    private String district;
    private String village;
}
