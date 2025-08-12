package com.startup.tasteflowbe.enums;


/**
 * Đại diện trạng thái của yêu cầu đổi/trả hàng (ReturnRequest):
 * PENDING → Chờ xử lý.
 * APPROVED → Đã được chấp nhận (chờ hoàn tiền/đổi hàng).
 * REJECTED → Bị từ chối.
 * CLOSED → Đã hoàn tất, không cần xử lý thêm. (refund xong, đổi hàng xong).
 */
public enum ReturnStatus {
    PENDING, APPROVED, REJECTED, CLOSED
}
