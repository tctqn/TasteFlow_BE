package com.startup.tasteflowbe.dto.response;

import lombok.Data;

@Data
public class ReturnAttachmentResponseDTO {
    private Long id;
    private Long returnItemId; // có thể null
    private String fileUrl;
    private String fileType;
}