package com.startup.tasteflowbe.enums;

public enum OrderStatus {
    PENDING,     // Đặt hàng xong
    PAID,        // Đã thanh toán
    CONFIRMED,   // Đã xác nhận
    PROCESSING,  // Đang chuẩn bị
    SHIPPING,    // Đang giao hàng
    DELIVERED,   // Giao thành công
    CANCELLED,   // Đã hủy
    FAILED       // Thất bại (ví dụ như fail thanh toán)
}
