package com.startup.tasteflowbe.enums;

/**
 Đại diện tình trạng hàng khi trả:
 SEALED → Còn nguyên seal, chưa mở.
 OPENED → Đã mở nhưng không hỏng.
 DAMAGED → Bị hỏng, vỡ, móp méo, quá hạn.
 */
public enum ItemCondition {
    SEALED, OPENED, DAMAGED
}
