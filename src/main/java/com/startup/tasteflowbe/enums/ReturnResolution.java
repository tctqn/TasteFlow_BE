package com.startup.tasteflowbe.enums;

/**
 *
 Đại diện cách xử lý cho từng sản phẩm trong yêu cầu đổi/trả (ReturnItem):
 REFUND → Hoàn tiền.
 EXCHANGE → Đổi lấy sản phẩm khác/mới.
 DISCARD → Hàng bỏ đi, không nhập lại kho.
 */
public enum ReturnResolution {
    REFUND, EXCHANGE, DISCARD
}
