package com.startup.tasteflowbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreProductDTO {
    private Long productId; // Mã sản phẩm
    private String sku; // Mã sản phẩm duy nhất
    private String productName; // Tên sản phẩm
    private String unitName; // Đơn vị cơ bản quản lý tồn kho
    private Integer totalQuantity; // Tổng số lượng tồn
    private Integer totalBatches; // Số lô đang tồn
    private Double salePrice; // Giá bán
    private Integer reorderLevel; // Ngưỡng cảnh báo nhập hàng lại
    private String status; // Trạng thái hàng hóa
    private String expiryStatus; // Trạng thái hạn sử dụng
}

