package com.startup.tasteflowbe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_storage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "reference_table", nullable = false, length = 100)
    private String referenceTable; // Ví dụ: "voucher", "product", "user"

    @Column(name = "reference_id", nullable = false)
    private Long referenceId; // ID của bản ghi trong bảng tương ứng

    @Column(name = "description")
    private String description; // Mô tả ảnh nếu cần

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
